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
    public ScheduleViewDTO getSchedule() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        List<Schedule> schedules = scheduleDao.findByUser(user);
        System.out.println("Schedule count: " + schedules.size());

        if (schedules.isEmpty()) {
            return null;
        }

        // Get the most recent schedule
        Schedule latestSchedule = schedules.get(schedules.size() - 1);

        // Use the mapper to convert the Schedule entity to ScheduleViewDTO
        return mapper.toScheduleViewDto(latestSchedule);
    }

    @Override
    @Transactional
    public void saveSchedule(ScheduleDTO dto) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        Term term = termDao.findById(dto.getTermId()).orElse(null);
        Schedule schedule = new Schedule();
        schedule.setUser(user);
        schedule.setExamType(dto.getExamType());
        schedule.setGeneratedAt(LocalDateTime.parse(dto.getGeneratedAt()));
        schedule.setTerm(term);

        List<Session> sessions = new ArrayList<>();
        for (SessionDTO sDto : dto.getStudyPlan()) {
            Session session = mapper.toSession(sDto);
            session.setIsCompleted(false);
            session.setSchedule(schedule);
            session.setIsScheduled(sDto.getIsScheduled());

            // Set course and topic references if needed
            if (sDto.getCourseId() != null) {
                session.setCourse(courseDao.findById(sDto.getCourseId()));
            }
            if (sDto.getTopicId() != null) {
                session.setTopic(topicDao.findById(sDto.getTopicId()));
            }
            if (sDto.getAssignmentId() != null) {
                session.setAssignment(assignmentDao.findById(sDto.getAssignmentId()));
            }

            sessions.add(session);
        }

        schedule.setSessions(sessions);
        scheduleDao.save(schedule); // Cascade saves sessions
        System.out.println(dto);
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
}
