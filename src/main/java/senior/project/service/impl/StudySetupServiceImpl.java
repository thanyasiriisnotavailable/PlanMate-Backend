package senior.project.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.*;
import senior.project.dto.*;
import senior.project.dto.plan.StudySetupDTO;
import senior.project.entity.*;
import senior.project.service.StudySetupService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

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
    @Transactional
    public void processStudySetup(StudySetupDTO dto) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        Term term = new Term();
        term.setUser(user);
        term.setName(dto.getTerm().getName());
        term.setStartDate(dto.getTerm().getStartDate());
        term.setEndDate(dto.getTerm().getEndDate());
        Term persistedTerm = termDao.save(term); // Step 1: Save Term to generate its ID.

        if (dto.getTerm().getCourses() != null) {
            for (CourseResponseDTO courseDTO : dto.getTerm().getCourses()) {
                Course course = new Course();

                // ============================ BUG FIX AREA ============================
                // The error "Column 'course_code' cannot be null" was caused by trying to
                // access the course code via `getCourseCode()` when the incoming JSON field
                // is named "id".
                //
                // Corrected Logic:
                // Changed `courseDTO.getCourseCode()` to `courseDTO.getId()` to correctly
                // retrieve the value (e.g., "C101") from the DTO.
                CourseId courseId = new CourseId(persistedTerm.getTermId(), courseDTO.getCourseCode());
                // ======================================================================

                course.setCourseId(courseId);
                course.setName(courseDTO.getName());
                course.setCredit(courseDTO.getCredit());
                course.setTerm(persistedTerm);

                Course persistedCourse = courseDao.save(course);

                List<Topic> savedTopics = new ArrayList<>();
                if (courseDTO.getTopics() != null) {
                    for (TopicDTO topicDTO : courseDTO.getTopics()) {
                        Topic topic = mapper.toTopic(topicDTO);
                        topic.setId(UUID.randomUUID().toString()); // Set ID before saving
                        topic.setCourse(persistedCourse);
                        savedTopics.add(topicDao.save(topic));
                    }
                }

                if (courseDTO.getExams() != null) {
                    for (ExamDTO examDTO : courseDTO.getExams()) {
                        Exam exam = mapper.toExam(examDTO);
                        exam.setId(UUID.randomUUID().toString()); // Set ID before saving
                        exam.setCourse(persistedCourse);
                        examDao.save(exam);
                    }
                }

                if (courseDTO.getAssignments() != null) {
                    for (AssignmentDTO assignmentDTO : courseDTO.getAssignments()) {
                        Assignment assignment = mapper.dtoToAssignment(assignmentDTO);
                        assignment.setId(UUID.randomUUID().toString()); // Set ID before saving
                        assignment.setCourse(persistedCourse);

                        if (assignmentDTO.getAssociatedTopicIds() != null && !assignmentDTO.getAssociatedTopicIds().isEmpty()) {
                            List<Topic> topicsToAssociate = savedTopics.stream()
                                    .filter(topic -> assignmentDTO.getAssociatedTopicIds().contains(topic.getId()))
                                    .collect(Collectors.toList());
                            assignment.setAssociatedTopics(topicsToAssociate);
                        }
                        assignmentDao.save(assignment);
                    }
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

    @Override
    @Transactional // Ensure transactions for operations involving multiple DAOs
    public TermResponseDTO getTermById(Long termId) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        // Ensure the term belongs to the authenticated user
        String userUid = SecurityUtil.getAuthenticatedUid();
        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot access another user's term.");
        }

        // Eagerly load courses and their sub-entities
        List<Course> courses = courseDao.findByTerm(term);
        courses.forEach(course -> {
            course.getTopics().clear();
            course.getTopics().addAll(topicDao.findByCourse(course));

            course.getAssignments().clear();
            course.getAssignments().addAll(assignmentDao.findByCourse(course));

            course.getExams().clear();
            course.getExams().addAll(examDao.findByCourse(course));
        });
        term.setCourses(courses);

        return mapper.toTermDto(term);
    }

    @Override
    public TermResponseDTO getCurrentTerm() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        Optional<Term> currentTermOpt = termDao.getCurrentTerm();

        if (currentTermOpt.isEmpty()) return null;

        Term currentTerm = currentTermOpt.get();
        // Optional: verify if this term belongs to the current user
        if (!currentTerm.getUser().equals(user)) return null;

        return mapper.toTermDto(currentTerm);
    }

    @Override
    @Transactional
    public TermResponseDTO saveTerm(TermRequestDTO termDTO) {
        User user = fetchUser();
        Term term = mapper.toTerm(termDTO, user);
        // New terms don't have an ID yet, so they are always created.
        term.setCourses(new ArrayList<>()); // Initialize an empty list of courses
        Term savedTerm = termDao.save(term);
        return mapper.toTermDto(savedTerm);
    }

    @Override
    @Transactional
    public TermResponseDTO updateTerm(TermRequestDTO request, Long id) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = fetchUser();

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
            course.getTopics().clear();
            course.getTopics().addAll(topicDao.findByCourse(course));

            course.getAssignments().clear();
            course.getAssignments().addAll(assignmentDao.findByCourse(course));

            course.getExams().clear();
            course.getExams().addAll(examDao.findByCourse(course));
        });
        saved.setCourses(courses);

        return mapper.toTermDto(saved);
    }

    @Override
    @Transactional
    public List<CourseResponseDTO> saveAllCourses(Long termId, List<CourseResponseDTO> courseDTOs) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        String userUid = SecurityUtil.getAuthenticatedUid();
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

            // Re-set the collections on the savedCourse to ensure they are up-to-date
            savedCourse.setTopics(topicDao.findByCourse(savedCourse));
            savedCourse.setAssignments(assignmentDao.findByCourse(savedCourse));
            savedCourse.setExams(examDao.findByCourse(savedCourse));
        }
        return mapper.toCourseResponseDtoList(savedCourses);
    }

    @Override
    @Transactional
    public void deleteCourse(Long termId, String courseCode) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        String userUid = SecurityUtil.getAuthenticatedUid();
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

    @Override
    @Transactional
    public CourseResponseDTO getCourseDetails(Long termId, String courseCode) {
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        // Ensure the term belongs to the authenticated user
        String userUid = SecurityUtil.getAuthenticatedUid();
        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot access another user's term details.");
        }

        CourseId courseId = new CourseId(termId, courseCode);
        Course course = courseDao.findById(courseId); // Assumes findById returns directly, or needs .orElseThrow()
        // Make sure your CourseDao.findById(CourseId) exists and returns a Course

        if (course == null) {
            throw new NoSuchElementException("Course not found with code: " + courseCode + " in term: " + termId);
        }

        // Eagerly load sub-entities if not already done by JPA config (e.g., fetch=EAGER or with @Transactional and then accessing them)
        // If not eagerly fetched by default, explicitly load them to avoid N+1 issues or lazy initialization exceptions.
        course.getTopics().clear();
        course.getTopics().addAll(topicDao.findByCourse(course));
        course.getAssignments().clear();
        course.getAssignments().addAll(assignmentDao.findByCourse(course));
        course.getExams().clear();
        course.getExams().addAll(examDao.findByCourse(course));

        return mapper.toCourseResponseDto(course);
    }

    // ===================================================================================
    // === COURSE DETAILS UPDATE LOGIC (THE PRIMARY FIX) ===
    // ===================================================================================

    @Override
    @Transactional
    public CourseResponseDTO updateCourseDetails(Long termId, String courseCode, CourseResponseDTO details) {
        System.out.println("\n\n--- [START] updateCourseDetails for TermID: " + termId + ", CourseCode: " + courseCode + " ---");

        CourseId courseId = new CourseId(termId, courseCode);
        Course course = courseDao.findById(courseId);

        if (course == null) {
            System.err.println("[FATAL] Course could not be found with TermID: " + termId + " and CourseCode: " + courseCode);
            throw new NoSuchElementException("Course not found with code: " + courseCode);
        }
        System.out.println("[DEBUG] Found course: " + course.getName());

        String userUid = SecurityUtil.getAuthenticatedUid();
        if (!course.getTerm().getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot update another user's course.");
        }

        // Delegate to helper methods for smart updates. The order is important.
        System.out.println("\n[DEBUG] --- Starting to process children entities ---");
        Map<String, Topic> topicMap = saveTopics(details.getTopics(), course);
        saveExams(details.getExams(), course);
        saveAssignments(details.getAssignments(), course, topicMap);

        // Explicitly flush changes to the database.
        System.out.println("\n[DEBUG] Flushing changes to the database...");
        courseDao.flush();
        System.out.println("[SUCCESS] Transaction flushed.");

        // Reload the course to ensure all collections are fresh before mapping to DTO
        Course updatedCourse = courseDao.findById(courseId);
        updatedCourse.getTopics().size();       // Trigger lazy loading
        updatedCourse.getAssignments().size();  // Trigger lazy loading
        updatedCourse.getExams().size();        // Trigger lazy loading

        System.out.println("--- [END] updateCourseDetails successful ---\n\n");
        return mapper.toCourseResponseDto(updatedCourse);
    }


    // ===================================================================================
    // === HELPER METHODS WITH DEBUG LOGGING ===
    // ===================================================================================

    private Map<String, Topic> saveTopics(List<TopicDTO> topicDTOs, Course course) {
        System.out.println("\n[DEBUG] --- Saving Topics ---");
        if (topicDTOs == null) {
            System.out.println("[DEBUG] Topic list is null. Deleting all topics for course: " + course.getName());
            topicDao.deleteByCourse(course);
            return Collections.emptyMap();
        }

        Map<String, Topic> existingTopics = topicDao.findByCourse(course).stream()
                .collect(Collectors.toMap(Topic::getId, topic -> topic));
        System.out.println("[DEBUG] Found " + existingTopics.size() + " existing topics in the database.");

        Set<String> incomingTopicIds = topicDTOs.stream().map(TopicDTO::getId).collect(Collectors.toSet());
        Map<String, Topic> processedTopics = new HashMap<>();

        for (TopicDTO dto : topicDTOs) {
            Topic topic;
            if (existingTopics.containsKey(dto.getId())) {
                System.out.println("[DEBUG] Updating existing topic: ID=" + dto.getId() + ", Name=" + dto.getName());
                topic = existingTopics.get(dto.getId());
                mapper.updateTopicFromDto(dto, topic);
            } else {
                System.out.println("[DEBUG] Creating new topic: ID=" + dto.getId() + ", Name=" + dto.getName());
                topic = mapper.toTopic(dto);
                topic.setCourse(course);
            }
            Topic savedTopic = topicDao.save(topic);
            processedTopics.put(savedTopic.getId(), savedTopic);
        }

        // Delete topics that are no longer in the incoming DTO list
        for (Topic existingTopic : existingTopics.values()) {
            if (!incomingTopicIds.contains(existingTopic.getId())) {
                System.out.println("[DEBUG] Deleting obsolete topic: ID=" + existingTopic.getId() + ", Name=" + existingTopic.getName());
                topicDao.deleteById(existingTopic.getId());
            }
        }
        System.out.println("[SUCCESS] Topic saving process complete. " + processedTopics.size() + " topics processed.");
        return processedTopics;
    }

    private void saveExams(List<ExamDTO> examDTOs, Course course) {
        System.out.println("\n[DEBUG] --- Saving Exams ---");
        if (examDTOs == null) {
            System.out.println("[DEBUG] Exam list is null. Deleting all exams for course: " + course.getName());
            examDao.deleteByCourse(course);
            return;
        }

        Map<String, Exam> existingExams = examDao.findByCourse(course).stream()
                .collect(Collectors.toMap(Exam::getId, exam -> exam));
        System.out.println("[DEBUG] Found " + existingExams.size() + " existing exams in the database.");

        Set<String> incomingExamIds = examDTOs.stream().map(ExamDTO::getId).collect(Collectors.toSet());

        for (ExamDTO dto : examDTOs) {
            Exam exam;
            if (existingExams.containsKey(dto.getId())) {
                System.out.println("[DEBUG] Updating existing exam: ID=" + dto.getId() + ", Type=" + dto.getType());
                exam = existingExams.get(dto.getId());
                mapper.updateExamFromDto(dto, exam);
            } else {
                System.out.println("[DEBUG] Creating new exam: ID=" + dto.getId() + ", Type=" + dto.getType());
                exam = mapper.toExam(dto);
                exam.setCourse(course);
            }
            examDao.save(exam);
        }

        for (Exam existingExam : existingExams.values()) {
            if (!incomingExamIds.contains(existingExam.getId())) {
                System.out.println("[DEBUG] Deleting obsolete exam: ID=" + existingExam.getId() + ", Type=" + existingExam.getType());
                examDao.delete(existingExam);
            }
        }
        System.out.println("[SUCCESS] Exam saving process complete.");
    }

    private void saveAssignments(List<AssignmentDTO> assignmentDTOs, Course course, Map<String, Topic> topicMap) {
        System.out.println("\n[DEBUG] --- Saving Assignments ---");
        if (assignmentDTOs == null) {
            System.out.println("[DEBUG] Assignment list is null. Deleting all assignments for course: " + course.getName());
            assignmentDao.deleteByCourse(course);
            return;
        }

        Map<String, Assignment> existingAssignments = assignmentDao.findByCourse(course).stream()
                .collect(Collectors.toMap(Assignment::getId, assignment -> assignment));
        System.out.println("[DEBUG] Found " + existingAssignments.size() + " existing assignments in the database.");

        Set<String> incomingAssignmentIds = assignmentDTOs.stream().map(AssignmentDTO::getId).collect(Collectors.toSet());

        for (AssignmentDTO dto : assignmentDTOs) {
            Assignment assignment;
            if (existingAssignments.containsKey(dto.getId())) {
                System.out.println("[DEBUG] Updating existing assignment: ID=" + dto.getId() + ", Name=" + dto.getName());
                assignment = existingAssignments.get(dto.getId());
                mapper.toAssignment(dto, assignment);
            } else {
                System.out.println("[DEBUG] Creating new assignment: ID=" + dto.getId() + ", Name=" + dto.getName());
                assignment = mapper.toAssignment(dto, new Assignment());
                assignment.setCourse(course);
            }

            // --- Robust Topic Linking ---
            List<Topic> associatedTopics = new ArrayList<>();
            if (dto.getAssociatedTopicIds() != null && !dto.getAssociatedTopicIds().isEmpty()) {
                System.out.println("[DEBUG] Linking topics for assignment '" + dto.getName() + "'. Provided Topic IDs: " + dto.getAssociatedTopicIds());
                for (String topicId : dto.getAssociatedTopicIds()) {
                    Topic topic = topicMap.get(topicId);
                    if (topic != null) {
                        associatedTopics.add(topic);
                        System.out.println("  [SUCCESS] Found and linked Topic ID: " + topicId);
                    } else {
                        System.err.println("  [WARNING] Topic ID '" + topicId + "' NOT FOUND in the topic map. It will not be linked.");
                    }
                }
            }
            assignment.setAssociatedTopics(associatedTopics);
            // --- End Linking ---

            assignmentDao.save(assignment);
        }

        for (Assignment existingAssignment : existingAssignments.values()) {
            if (!incomingAssignmentIds.contains(existingAssignment.getId())) {
                System.out.println("[DEBUG] Deleting obsolete assignment: ID=" + existingAssignment.getId() + ", Name=" + existingAssignment.getName());
                assignmentDao.delete(existingAssignment);
            }
        }
        System.out.println("[SUCCESS] Assignment saving process complete.");
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
    public StudySetupDTO getStudySetup() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = fetchUser();
        Term term = termDao.findByUser(user);

        if (term == null) return null;

        List<Course> courses = courseDao.findByTerm(term);
        courses.forEach(course -> {
            course.getTopics().clear();
            course.getTopics().addAll(topicDao.findByCourse(course));

            course.getAssignments().clear();
            course.getAssignments().addAll(assignmentDao.findByCourse(course));

            course.getExams().clear();
            course.getExams().addAll(examDao.findByCourse(course));
        });

        term.setCourses(courses);

        return StudySetupDTO.builder()
                .userUid(userUid)
                .term(mapper.toTermDto(term))
                .availabilities(mapper.toAvailabilityDtoList(availabilityDao.findByUser(user)))
                .build();
    }

    // --- Helper Methods ---
    private User fetchUser() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        return userDao.findByUid(userUid);
    }
}