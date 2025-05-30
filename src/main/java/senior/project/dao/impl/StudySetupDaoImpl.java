package senior.project.dao.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.StudySetupDao;
import senior.project.dto.*;
import senior.project.entity.*;
import senior.project.repository.*;
import senior.project.util.DTOMapper;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudySetupDaoImpl implements StudySetupDao {

    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final DTOMapper mapper;

    @Transactional
    public void processStudySetup(StudySetupDTO dto) {
        User user = userRepository.findByUid(dto.getUserUid());

        Term term = mapper.toTerm(dto.getTerm(), user);
        term.setUser(user);
        term = termRepository.save(term);

        for (CourseDTO courseDTO : dto.getTerm().getCourses()) {
            Course course = mapper.toCourse(courseDTO);
            course.setTerm(term);
            course = courseRepository.save(course);

            List<Topic> savedTopics = new ArrayList<>();
            for (TopicDTO topicDTO : courseDTO.getTopics()) {
                Topic topic = mapper.toTopic(topicDTO);
                topic.setCourse(course);
                savedTopics.add(topicRepository.save(topic));
            }

            for (ExamDTO examDTO : courseDTO.getExams()) {
                Exam exam = mapper.toExam(examDTO);
                exam.setCourse(course);
                examRepository.save(exam);
            }

            if (courseDTO.getAssignments() != null) {
                for (AssignmentDTO assignmentDTO : courseDTO.getAssignments()) {
                    Assignment assignment = mapper.toAssignment(assignmentDTO);
                    assignment.setCourse(course);

                    if (assignmentDTO.getAssociatedTopicIndex() != null &&
                            assignmentDTO.getAssociatedTopicIndex() < savedTopics.size()) {
                        assignment.setAssociatedTopic(savedTopics.get(assignmentDTO.getAssociatedTopicIndex()));
                    }

                    assignmentRepository.save(assignment);
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
            availabilityRepository.save(availability);
        }
    }
}