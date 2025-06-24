package senior.project.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.*;
import senior.project.dto.*;
import senior.project.entity.*;
import senior.project.service.StudySetupService;
import senior.project.util.DTOMapper;

import java.time.LocalDate;
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
    @Transactional // Add @Transactional if you need to fetch associated collections lazily
    public TermResponseDTO getTermById(String userUid, Long termId) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        // Ensure the term belongs to the authenticated user
        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot access another user's term.");
        }

        List<Course> courses = courseDao.findByTerm(term);
        courses.forEach(course -> {
            course.setTopics(topicDao.findByCourse(course));
            course.setAssignments(assignmentDao.findByCourse(course));
            course.setExams(examDao.findByCourse(course));
        });
        term.setCourses(courses); // Make sure the entity has its courses set for mapping

        return mapper.toTermDto(term);
    }

    @Override
    public TermResponseDTO getCurrentTerm(String uid) {
        User user = userDao.findByUid(uid);
        Optional<Term> currentTermOpt = termDao.getCurrentTerm();

        if (currentTermOpt.isEmpty()) return null;

        Term currentTerm = currentTermOpt.get();
        // Optional: verify if this term belongs to the current user
        if (!currentTerm.getUser().equals(user)) return null;

        return mapper.toTermDto(currentTerm);
    }

    @Override
    @Transactional
    public TermResponseDTO saveTerm(String userUid, TermRequestDTO termDTO) {
        User user = fetchUser(userUid);
        Term term = mapper.toTerm(termDTO, user);
        term.setUser(user);
        term.setCourses(new ArrayList<>());
        Term savedTerm = termDao.save(term);
        return mapper.toTermDto(savedTerm);
    }

    @Override
    @Transactional
    public TermResponseDTO updateTerm(String userUid, TermRequestDTO request, Long id) {
        User user = fetchUser(userUid);

        Term term = termDao.findById(id)
                .orElseThrow();

        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot update another user's term.");
        }

        term.setName(request.getName());
        term.setStartDate(LocalDate.parse(request.getStartDate()));
        term.setEndDate(LocalDate.parse(request.getEndDate()));

        Term saved = termDao.save(term);
        return mapper.toTermDto(saved);
    }

    @Override
    @Transactional
    public void saveCourses(String userUid, List<CourseDTO> courseDTOs) {
        Term term = fetchUserTerm(userUid);
        for (CourseDTO courseDTO : courseDTOs) {
            Course course = new Course();

            course.setTerm(term);

            // Then create and set the composite ID
            CourseId courseId = new CourseId(term.getTermId(), courseDTO.getCourseCode());
            course.setCourseId(courseId);

            course.setName(courseDTO.getName());
            course.setCredit(courseDTO.getCredit());

            courseDao.save(course);
        }
    }

    @Override
    @Transactional
    public void saveCourseDetails(String userUid, List<CourseDTO> courseDTOs) {
        Term term = fetchUserTerm(userUid);

        for (CourseDTO courseDTO : courseDTOs) {
            String courseCode = Optional.ofNullable(courseDTO.getCourseCode())
                    .orElseThrow(() -> new IllegalArgumentException("courseCode is required in CourseDTO."));

            CourseId courseId = new CourseId(term.getTermId(), courseCode);
            Course course = Optional.ofNullable(courseDao.findById(courseId))
                    .orElseThrow(() -> new IllegalStateException("Course not found: " + courseId));

            Map<String, Topic> topicMap = new HashMap<>();

            saveTopics(courseDTO.getTopics(), course, topicMap);
            saveExams(courseDTO.getExams(), course);
            saveAssignments(courseDTO.getAssignments(), course, topicMap);
        }
    }

    @Override
    @Transactional
    public void saveAvailabilities(String userUid, List<AvailabilityDTO> availabilityDTOs) {
        User user = fetchUser(userUid);
        for (AvailabilityDTO dto : availabilityDTOs) {
            Availability availability = Availability.builder()
                    .user(user)
                    .date(dto.getDate())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .build();
            availabilityDao.save(availability);
        }
    }

    @Override
    @Transactional
    public StudySetupResponseDTO getStudySetup(String userUid) {
        User user = fetchUser(userUid);
        Term term = termDao.findByUser(user);

        if (term == null) return null;

        List<Course> courses = courseDao.findByTerm(term);
        courses.forEach(course -> {
            course.setTopics(topicDao.findByCourse(course));
            course.setAssignments(assignmentDao.findByCourse(course));
            course.setExams(examDao.findByCourse(course));
        });

        term.setCourses(courses);

        return StudySetupResponseDTO.builder()
                .userUid(userUid)
                .term(mapper.toTermDto(term))
                .availabilities(mapper.toAvailabilityDtoList(availabilityDao.findByUser(user)))
                .build();
    }

    // --- Helper Methods ---
    private User fetchUser(String userUid) {
        return Optional.ofNullable(userDao.findByUid(userUid))
                .orElseThrow(() -> new IllegalStateException("User not found: " + userUid));
    }

    private Term fetchUserTerm(String userUid) {
        User user = fetchUser(userUid);
        return Optional.ofNullable(termDao.findByUser(user))
                .orElseThrow(() -> new IllegalStateException("Term must be saved before saving courses."));
    }

    private void saveTopics(List<TopicDTO> topicDTOs, Course course, Map<String, Topic> topicMap) {
        if (topicDTOs == null) return;
        for (TopicDTO dto : topicDTOs) {
            Topic topic = mapper.toTopic(dto);
            topic.setCourse(course);
            Topic saved = topicDao.save(topic);
            topicMap.put(saved.getId(), saved);
        }
    }

    private void saveExams(List<ExamDTO> examDTOs, Course course) {
        if (examDTOs == null) return;
        for (ExamDTO dto : examDTOs) {
            Exam exam = mapper.toExam(dto);
            exam.setCourse(course);
            examDao.save(exam);
        }
    }

    private void saveAssignments(List<AssignmentDTO> assignmentDTOs, Course course, Map<String, Topic> topicMap) {
        if (assignmentDTOs == null) return;
        for (AssignmentDTO dto : assignmentDTOs) {
            Assignment assignment = mapper.toAssignment(dto);
            assignment.setCourse(course);

            if (dto.getAssociatedTopicIds() != null) {
                List<Topic> associated = new ArrayList<>();
                for (String topicId : dto.getAssociatedTopicIds()) {
                    Topic topic = topicDao.findById(topicId);
                    if (topic != null) {
                        associated.add(topic);
                    } else {
                        System.out.println("Warning: Topic ID not found for assignment: " + topicId);
                    }
                }
                assignment.setAssociatedTopics(associated);
            }

            assignmentDao.save(assignment);
        }
    }
}