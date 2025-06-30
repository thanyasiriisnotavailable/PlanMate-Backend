package senior.project.service.impl;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import senior.project.dao.*;
import senior.project.dto.plan.GeneratePlanRequestDTO;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.dto.plan.GeneratePlanResponseDTO;
import senior.project.dto.plan.StudySetupDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.service.ScheduleService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleDao scheduleDao;
    private final UserDao userDao;
    private final CourseDao courseDao;
    private final TopicDao topicDao;
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
    public void saveSchedule(GeneratePlanResponseDTO dto) {
//        String userUid = SecurityUtil.getAuthenticatedUid();
//        User user = userDao.findByUid(userUid);
//        Schedule schedule = new Schedule();
//        schedule.setUser(user);
//
//        List<Session> sessions = new ArrayList<>();
//        for (SessionDTO sDto : dto.getStudy_plan()) {
//            Session session = mapper.toSession(sDto);
//            session.setSchedule(schedule);
//
//            // Set course and topic references if needed
//            if (sDto.getCourseId() != null) {
//                session.setCourse(courseDao.findById(sDto.getCourseId()));
//            }
//            if (sDto.getTopicId() != null) {
//                session.setTopic(topicDao.findById(sDto.getTopicId()));
//            }
//
//            sessions.add(session);
//        }
//
//        schedule.setSessions(sessions);
//        scheduleDao.save(schedule); // Cascade saves sessions
        System.out.println(dto);
    }

    @Override
    public GeneratePlanResponseDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO) {
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
            ResponseEntity<GeneratePlanResponseDTO> response = restTemplate.exchange(
                    "http://localhost:8000/api/generate-plan/", // Your FastAPI endpoint
                    HttpMethod.POST,
                    request,
                    GeneratePlanResponseDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
