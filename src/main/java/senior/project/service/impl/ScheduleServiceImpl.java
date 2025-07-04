package senior.project.service.impl;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import senior.project.dao.*;
import senior.project.dto.CourseResponseDTO;
import senior.project.dto.TopicDTO;
import senior.project.dto.plan.*;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.Term;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.enums.ExamType;
import senior.project.service.ScheduleService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleDao scheduleDao;
    private final UserDao userDao;
    private final CourseDao courseDao;
    private final TopicDao topicDao;
    private final TermDao termDao;
    private final AssignmentDao assignmentDao;
    private final DTOMapper mapper;
    private final StudyPreferenceDao studyPreferenceDao;


    @Override
    public ScheduleDTO getSchedule() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        List<Schedule> schedules = scheduleDao.findByUser(user);
        System.out.println("Schedule count: " + schedules.size());

        if (schedules.isEmpty()) {
            return null; // No schedule found
        }

        // Get the most recent schedule
        Schedule latestSchedule = schedules.get(schedules.size() - 1);

        // Manually build the DTO to separate scheduled and unscheduled plans
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setId(latestSchedule.getId());
        scheduleDTO.setGeneratedAt(latestSchedule.getGeneratedAt().toString());
        scheduleDTO.setExamType(latestSchedule.getExamType());
        scheduleDTO.setTermId(latestSchedule.getTerm().getTermId());

        List<SessionDTO> studyPlan = new ArrayList<>();
        List<SessionDTO> unscheduledPlan = new ArrayList<>();

        for (Session session : latestSchedule.getSessions()) {
            SessionDTO sessionDTO = mapper.toSessionDto(session); // Use your existing mapper for individual sessions
            if (session.getIsScheduled()) {
                studyPlan.add(sessionDTO);
            } else {
                unscheduledPlan.add(sessionDTO);
            }
        }

        scheduleDTO.setStudyPlan(studyPlan);
        scheduleDTO.setUnscheduledPlan(unscheduledPlan);

        return scheduleDTO;
    }

    @Override
    @Transactional
    public ScheduleDTO saveSchedule(ScheduleDTO dto) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        Term term = termDao.findById(dto.getTermId()).orElse(null);
        Schedule schedule = new Schedule();
        schedule.setId(dto.getId());
        schedule.setUser(user);
        schedule.setExamType(dto.getExamType());
        schedule.setGeneratedAt(LocalDateTime.parse(dto.getGeneratedAt()));
        schedule.setTerm(term);

        List<Session> allSessions = new ArrayList<>();

        // Process the scheduled sessions from the study plan
        if (dto.getStudyPlan() != null) {
            for (SessionDTO sDto : dto.getStudyPlan()) {
                // isScheduled is inherently true for this list
                allSessions.add(mapSessionDtoToEntity(sDto, schedule));
            }
        }

        // Process the unscheduled sessions
        if (dto.getUnscheduledPlan() != null) {
            for (SessionDTO sDto : dto.getUnscheduledPlan()) {
                // isScheduled is inherently false for this list
                allSessions.add(mapSessionDtoToEntity(sDto, schedule));
            }
        }

        schedule.setSessions(allSessions);
        Schedule savedSchedule = scheduleDao.save(schedule); // Cascade saves sessions

        // Return a correctly formatted DTO by calling the updated getSchedule logic
        return getSchedule();
    }

    @Override
    @Transactional
    public ScheduleDTO updateSchedule(ScheduleDTO dto) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        Schedule schedule = scheduleDao.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + dto.getId()));

        if (!schedule.getUser().equals(user)) {
            throw new SecurityException("Access denied to update this schedule.");
        }

        // Update schedule metadata
        Term term = termDao.findById(dto.getTermId()).orElse(null);
        schedule.setTerm(term);
        schedule.setGeneratedAt(LocalDateTime.parse(dto.getGeneratedAt()));
        schedule.setExamType(dto.getExamType());

        // Clear the old list of sessions to replace it with the new one
        schedule.getSessions().clear();

        List<Session> allSessions = new ArrayList<>();

        // Process the scheduled sessions from the study plan
        if (dto.getStudyPlan() != null) {
            for (SessionDTO sDto : dto.getStudyPlan()) {
                allSessions.add(mapSessionDtoToEntity(sDto, schedule));
            }
        }

        // Process the unscheduled sessions
        if (dto.getUnscheduledPlan() != null) {
            for (SessionDTO sDto : dto.getUnscheduledPlan()) {
                allSessions.add(mapSessionDtoToEntity(sDto, schedule));
            }
        }

        schedule.getSessions().addAll(allSessions);
        scheduleDao.save(schedule);

        // Return a correctly formatted DTO by calling the updated getSchedule logic
        return getSchedule();
    }

    @Override
    public ScheduleDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO) {
        String userUid = SecurityUtil.getAuthenticatedUid();

        // 1. Fetch the user's study preferences
        StudyPreference preference = studyPreferenceDao.findByUserUid(userUid);
        StudyPreferenceDTO preferences = mapper.toStudyPreferenceDto(preference);

        // 2. Create the consolidated request object
        GeneratePlanRequestDTO generationRequest = new GeneratePlanRequestDTO(
                preferences,
                setupDTO
        );

        // 3. Send request to FastAPI
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeneratePlanRequestDTO> request = new HttpEntity<>(generationRequest, headers);

        try {
            ResponseEntity<ScheduleDTO> response = restTemplate.exchange(
                    "http://localhost:8000/api/generate-plan/",
                    HttpMethod.POST,
                    request,
                    ScheduleDTO.class
            );
            ScheduleDTO result = response.getBody();

            // 🔽 Set additional fields before saving
            assert result != null;
            result.setGeneratedAt(LocalDateTime.now().toString()); // or use formatter
            result.setTermId(setupDTO.getTerm().getTermId());

            // Try to get the dominant exam type (e.g., from the first topic)
            ExamType examType = null;
            outer:
            for (CourseResponseDTO course : setupDTO.getTerm().getCourses()) {
                for (TopicDTO topic : course.getTopics()) {
                    if (topic.getExamType() != null) {
                        examType = topic.getExamType();
                        break outer;
                    }
                }
            }
            result.setExamType(examType);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Session mapSessionDtoToEntity(SessionDTO sDto, Schedule schedule) {
        Session session = mapper.toSession(sDto); // Assumes this maps basic fields
        session.setIsCompleted(false); // Default value
        session.setSchedule(schedule);

        // Set boolean based on DTO property
        session.setIsScheduled(sDto.getIsScheduled());

        // Set relational entities
        if (sDto.getCourseId() != null) {
            session.setCourse(courseDao.findById(sDto.getCourseId()));
        }
        if (sDto.getTopicId() != null) {
            session.setTopic(topicDao.findById(sDto.getTopicId()));
        }
        if (sDto.getAssignmentId() != null) {
            session.setAssignment(assignmentDao.findById(sDto.getAssignmentId()));
        }

        return session;
    }
}
