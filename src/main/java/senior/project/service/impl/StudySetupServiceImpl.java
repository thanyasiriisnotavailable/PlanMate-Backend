package senior.project.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.*;
import senior.project.dto.*;
import senior.project.entity.*;
import senior.project.service.StudySetupService;
import senior.project.util.DTOMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudySetupServiceImpl implements StudySetupService {

    private final UserDao userDao;
    private final TermDao termDao;
    private final CourseDao courseDao;
    private final TopicDao topicDao;
    private final ExamDao examDao;
    private final AssignmentDao assignmentDao;
    private final AvailabilityDao availabilityDao;
    private final DTOMapper mapper;

    @Override
    @Transactional
    public void processStudySetup(String userUid, StudySetupDTO dto) {
        User user = userDao.findByUid(userUid);

        Term term = mapper.toTerm(dto.getTerm(), user);
        term.setUser(user);
        termDao.save(term);

        for (CourseDTO courseDTO : dto.getTerm().getCourses()) {
            Course course = mapper.toCourse(courseDTO);
            course.setTerm(term);
            courseDao.save(course);

            List<Topic> savedTopics = new ArrayList<>();
            for (TopicDTO topicDTO : courseDTO.getTopics()) {
                Topic topic = mapper.toTopic(topicDTO);
                topic.setCourse(course);
                savedTopics.add(topicDao.save(topic));
            }

            for (ExamDTO examDTO : courseDTO.getExams()) {
                Exam exam = mapper.toExam(examDTO);
                exam.setCourse(course);
                examDao.save(exam);
            }

            if (courseDTO.getAssignments() != null) {
                for (AssignmentDTO assignmentDTO : courseDTO.getAssignments()) {
                    Assignment assignment = mapper.toAssignment(assignmentDTO);
                    assignment.setCourse(course);

                    if (assignmentDTO.getAssociatedTopicIndex() != null &&
                            assignmentDTO.getAssociatedTopicIndex() < savedTopics.size()) {
                        assignment.setAssociatedTopic(savedTopics.get(assignmentDTO.getAssociatedTopicIndex()));
                    }

                    assignmentDao.save(assignment);
                }
            }
        }

        for (AvailabilityDTO availabilityDTO : dto.getAvailabilities()) {
            Availability availability = Availability.builder()
                    .user(user)
                    .date(availabilityDTO.getDate())
                    .startTime(availabilityDTO.getStartTime())
                    .endTime(availabilityDTO.getEndTime())
                    .build();
            availabilityDao.save(availability);
        }
    }
}