package senior.project.service.impl;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import senior.project.dao.*;
import senior.project.dto.plan.*;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.service.ScheduleService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleDao scheduleDao;
    private final UserDao userDao;
    private final CourseDao courseDao;
    private final TopicDao topicDao;
    private final AssignmentDao assignmentDao;
    private final DTOMapper mapper;
    private final StudyPreferenceDao studyPreferenceDao;

    @Override
    @Transactional
    public ScheduleDTO getScheduleForUser() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        List<Schedule> schedules = scheduleDao.findByUser(user);

        if (schedules.isEmpty()) return null;

        Schedule latest = schedules.get(schedules.size() - 1);
        return mapper.toScheduleDto(latest);
    }

    @Override
    @Transactional
    public void saveSchedule(ScheduleDTO dto) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        Schedule schedule = new Schedule();
        schedule.setUser(user);

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

        // 3. Send the new DTO to FastAPI
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
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
