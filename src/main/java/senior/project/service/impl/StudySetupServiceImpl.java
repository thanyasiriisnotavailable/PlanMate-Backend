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
import java.util.stream.Collectors;

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
    @Transactional // Ensure transactions for operations involving multiple DAOs
    public TermResponseDTO getTermById(String userUid, Long termId) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        // Ensure the term belongs to the authenticated user
        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot access another user's term.");
        }

        // Eagerly load courses and their sub-entities
        List<Course> courses = courseDao.findByTerm(term);
        courses.forEach(course -> {
            course.setTopics(topicDao.findByCourse(course));
            course.setAssignments(assignmentDao.findByCourse(course));
            course.setExams(examDao.findByCourse(course));
        });
        term.setCourses(courses);

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
        // New terms don't have an ID yet, so they are always created.
        term.setCourses(new ArrayList<>()); // Initialize an empty list of courses
        Term savedTerm = termDao.save(term);
        return mapper.toTermDto(savedTerm);
    }

    @Override
    @Transactional
    public TermResponseDTO updateTerm(String userUid, TermRequestDTO request, Long id) {
        User user = fetchUser(userUid);

        Term term = termDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + id));

        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot update another user's term.");
        }

        term.setName(request.getName());
        term.setStartDate(LocalDate.parse(request.getStartDate()));
        term.setEndDate(LocalDate.parse(request.getEndDate()));

        Term saved = termDao.save(term);
        // Re-fetch courses and sub-entities to return a complete DTO after update
        List<Course> courses = courseDao.findByTerm(saved);
        courses.forEach(course -> {
            course.setTopics(topicDao.findByCourse(course));
            course.setAssignments(assignmentDao.findByCourse(course));
            course.setExams(examDao.findByCourse(course));
        });
        saved.setCourses(courses);

        return mapper.toTermDto(saved);
    }

    @Override
    @Transactional
    public List<CourseResponseDTO> saveAllCourses(String userUid, Long termId, List<CourseResponseDTO> courseDTOs) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot save courses for another user's term.");
        }

        List<Course> savedCourses = new ArrayList<>();
        List<Course> existingCoursesInTerm = courseDao.findByTerm(term);
        Set<CourseId> incomingCourseIds = courseDTOs.stream()
                .map(dto -> new CourseId(termId, dto.getCourseCode()))
                .collect(Collectors.toSet());

        // Delete courses that are in the database but not in the incoming DTO list
        for (Course existingCourse : existingCoursesInTerm) {
            CourseId existingCourseId = existingCourse.getCourseId();
            if (!incomingCourseIds.contains(existingCourseId)) {
                // Perform cascade delete if relationships are configured. Otherwise, delete related entities manually.
                topicDao.deleteByCourse(existingCourse);
                assignmentDao.deleteByCourse(existingCourse);
                examDao.deleteByCourse(existingCourse);
                courseDao.delete(existingCourse);
            }
        }


        for (CourseResponseDTO courseDTO : courseDTOs) {
            // Ensure the courseCode is present
            if (courseDTO.getCourseCode() == null || courseDTO.getCourseCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Course code cannot be empty for course: " + courseDTO.getName());
            }

            CourseId courseId = new CourseId(termId, courseDTO.getCourseCode());
            Optional<Course> existingCourseOpt = Optional.ofNullable(courseDao.findById(courseId));
            Course course;

            if (existingCourseOpt.isPresent()) {
                // Update existing course
                course = existingCourseOpt.get();
                mapper.updateCourseFromDto(courseDTO, course); // Use MapStruct to update properties
            } else {
                // Create new course
                course = mapper.toCourse(courseDTO, term); // Use default mapper for creation
            }
            course.setTerm(term); // Ensure the term is set

            Course savedCourse = courseDao.save(course);
            savedCourses.add(savedCourse);

            // Handle nested entities (topics, assignments, exams)
            // For simplicity, this example will just delete and re-create them.
            // A more robust solution might involve comparing and updating existing ones.

            // Delete existing sub-entities for this course before saving new ones
            topicDao.deleteByCourse(savedCourse);
            assignmentDao.deleteByCourse(savedCourse);
            examDao.deleteByCourse(savedCourse);

            // Save new sub-entities
            Map<String, Topic> topicMap = new HashMap<>(); // Re-create map for new topics
            saveTopics(courseDTO.getTopics(), savedCourse, topicMap);
            saveExams(courseDTO.getExams(), savedCourse);
            saveAssignments(courseDTO.getAssignments(), savedCourse, topicMap);

            // Re-set the collections on the savedCourse to ensure they are up-to-date
            savedCourse.setTopics(topicDao.findByCourse(savedCourse));
            savedCourse.setAssignments(assignmentDao.findByCourse(savedCourse));
            savedCourse.setExams(examDao.findByCourse(savedCourse));
        }
        return mapper.toCourseResponseDtoList(savedCourses);
    }

    @Override
    @Transactional
    public void deleteCourse(String userUid, Long termId, String courseCode) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot delete courses for another user's term.");
        }

        CourseId courseId = new CourseId(termId, courseCode);
        Course courseToDelete = courseDao.findById(courseId);

        // Ensure the course belongs to the specified term (already covered by fetching term first)
        if (!courseToDelete.getTerm().getTermId().equals(termId)) {
            throw new IllegalArgumentException("Course does not belong to the specified term.");
        }

        // Delete child entities first if not using cascade delete in JPA
        topicDao.deleteByCourse(courseToDelete);
        assignmentDao.deleteByCourse(courseToDelete);
        examDao.deleteByCourse(courseToDelete);

        courseDao.delete(courseToDelete);
    }


//    @Override
//    @Override
//    @Transactional
//    public void saveAvailabilities(String userUid, List<AvailabilityDTO> availabilityDTOs) {
//        User user = fetchUser(userUid);
//        // Clear existing availabilities for the user to support "overwrite" behavior
//        availabilityDao.deleteByUser(user);
//        for (AvailabilityDTO dto : availabilityDTOs) {
//            Availability availability = Availability.builder()
//                    .user(user)
//                    .date(dto.getDate())
//                    .startTime(dto.getStartTime())
//                    .endTime(dto.getEndTime())
//                    .build();
//            availabilityDao.save(availability);
//        }
//    }

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
        return userDao.findByUid(userUid);
    }

    private void saveTopics(List<TopicDTO> topicDTOs, Course course, Map<String, Topic> topicMap) {
        if (topicDTOs == null) return;
        for (TopicDTO dto : topicDTOs) {
            // If topic has an ID, it might be an update. Otherwise, it's new.
            Topic topic = (dto.getId() != null && topicDao.existsById(dto.getId()))
                    ? topicDao.findById(dto.getId())
                    : new Topic();
            mapper.toTopic(dto); // Map DTO properties to entity
            topic.setCourse(course);
            Topic saved = topicDao.save(topic);
            topicMap.put(saved.getId(), saved);
        }
    }

    private void saveExams(List<ExamDTO> examDTOs, Course course) {
        if (examDTOs == null) return;
        for (ExamDTO dto : examDTOs) {
            // Similar logic for exams: check for existing ID for updates
            Exam exam = (dto.getId() != null && examDao.existsById(dto.getId()))
                    ? examDao.findById(dto.getId())
                    : new Exam();
            mapper.toExam(dto); // Map DTO properties to entity
            exam.setCourse(course);
            examDao.save(exam);
        }
    }

    private void saveAssignments(List<AssignmentDTO> assignmentDTOs, Course course, Map<String, Topic> topicMap) {
        if (assignmentDTOs == null) return;
        for (AssignmentDTO dto : assignmentDTOs) {
            // Similar logic for assignments: check for existing ID for updates
            Assignment assignment = (dto.getId() != null && assignmentDao.existsById(dto.getId()))
                    ? assignmentDao.findById(dto.getId())
                    : new Assignment();
            mapper.toAssignment(dto); // Map DTO properties to entity
            assignment.setCourse(course);

            if (dto.getAssociatedTopicIds() != null) {
                List<Topic> associated = new ArrayList<>();
                for (String topicId : dto.getAssociatedTopicIds()) {
                    Topic topic = topicMap.get(topicId); // Try to get from current batch
                    if (topic == null) {
                        topic = topicDao.findById(topicId); // Else, try from DB
                    }
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