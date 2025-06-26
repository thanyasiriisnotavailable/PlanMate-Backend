package senior.project.service.impl;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import senior.project.dao.ScheduleDao;
import senior.project.dao.UserDao;
import senior.project.dao.CourseDao;
import senior.project.dao.TopicDao;
import senior.project.dto.plan.SessionDTO;
import senior.project.dto.plan.StudySetupResponseDTO;
import senior.project.entity.plan.Schedule;
import senior.project.entity.plan.Session;
import senior.project.entity.User;
import senior.project.util.DTOMapper;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.service.ScheduleService;
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
    private final DTOMapper mapper;

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
        for (SessionDTO sDto : dto.getStudy_plan()) {
            Session session = mapper.toSession(sDto);
            session.setSchedule(schedule);

            // Set course and topic references if needed
            if (sDto.getCourseId() != null) {
                session.setCourse(courseDao.findById(sDto.getCourseId()));
            }
            if (sDto.getTopicId() != null) {
                session.setTopic(topicDao.findById(sDto.getTopicId()));
            }

            sessions.add(session);
        }

        schedule.setSessions(sessions);
        scheduleDao.save(schedule); // Cascade saves sessions
    }

    @Override
    public ScheduleDTO generateScheduleFromFastAPI(StudySetupResponseDTO setupDTO) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<StudySetupResponseDTO> request = new HttpEntity<>(setupDTO, headers);

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