package senior.project.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.*;
import senior.project.dto.*;
import senior.project.dto.plan.StudySetupDTO;
import senior.project.entity.*;
import senior.project.exception.ValidationException;
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
    private static final long MAX_AVAILABILITY_DURATION_HOURS = 10;

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

                course.setCourseCode(courseDTO.getCourseCode());
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
    public TermResponseDTO saveTerm(TermRequestDTO termDTO, Long termId) {
        if (termDTO.getName().isEmpty()) {
            throw new ValidationException("The name of term cannot be empty.");
        }
        if (termDTO.getStartDate().isAfter(termDTO.getEndDate())) {
            throw new ValidationException("Start date cannot be after end date.");
        } else if (termDTO.getStartDate().isEqual(termDTO.getEndDate())) {
            throw new ValidationException("Start date cannot be equal to end date.");
        }

        User user = fetchUser();
        String userUid = user.getUid();
        Term term;

        // If termId is present, it's an update. Otherwise, it's a new term.
        if (termId != null) {
            // --- UPDATE LOGIC ---
            term = termDao.findById(termId)
                    .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

            // Security check: ensure the user owns the term
            if (!term.getUser().getUid().equals(userUid)) {
                throw new SecurityException("Unauthorized: Cannot update another user's term.");
            }
        } else {
            // --- CREATE LOGIC ---
            term = new Term();
            term.setUser(user);
            term.setCourses(new ArrayList<>()); // Initialize for a new term
        }

        // Map properties from DTO to the entity
        term.setName(termDTO.getName());
        term.setStartDate(termDTO.getStartDate());
        term.setEndDate(termDTO.getEndDate());

        Term savedTerm = termDao.save(term);

        // If it was an update, re-fetch associated entities to ensure the DTO is complete.
        // This part is often necessary if the relationships aren't eagerly fetched.
        if (termId != null) {
            List<Course> courses = courseDao.findByTerm(savedTerm);
            courses.forEach(course -> {
                course.getTopics().clear();
                course.getTopics().addAll(topicDao.findByCourse(course));
                course.getAssignments().clear();
                course.getAssignments().addAll(assignmentDao.findByCourse(course));
                course.getExams().clear();
                course.getExams().addAll(examDao.findByCourse(course));
            });
            savedTerm.setCourses(courses);
        }

        return mapper.toTermDto(savedTerm);
    }

    @Override
    @Transactional
    public List<CourseResponseDTO> saveAllCourses(Long termId, List<CourseResponseDTO> courseDTOs) {
        // Fetch Term and Validate Ownership
        Term term = termDao.findById(termId)
                .orElseThrow(() -> new NoSuchElementException("Term not found with ID: " + termId));

        String userUid = SecurityUtil.getAuthenticatedUid();
        if (!term.getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot modify courses for another user's term.");
        }

        // Perform validation on the incoming list *before* any DB operations
        validateCourseList(courseDTOs);

        // Handle deletions, updates, and creations
        Map<Long, Course> existingCoursesMap = courseDao.findByTerm(term).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));

        Set<Long> incomingCourseIds = courseDTOs.stream()
                .map(CourseResponseDTO::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Delete courses that are in the DB but not in the incoming list
        Set<Long> idsToDelete = new HashSet<>(existingCoursesMap.keySet());
        idsToDelete.removeAll(incomingCourseIds);
        if (!idsToDelete.isEmpty()) {
            courseDao.deleteAllByIdInBatch(idsToDelete); // More efficient deletion
        }

        // Iterate through DTOs to perform updates or creates
        List<Course> coursesToSave = new ArrayList<>();
        for (CourseResponseDTO dto : courseDTOs) {
            Course course;
            if (dto.getCourseId() != null && dto.getCourseId() != 0) {
                // Update existing course
                course = existingCoursesMap.get(dto.getCourseId());
                if (course == null) {
                    // This case should be rare but handles an invalid ID sent from the client
                    throw new NoSuchElementException("Cannot update. Course with ID " + dto.getCourseId() + " not found in this term.");
                }
            } else {
                // Create new course
                course = new Course();
                course.setTerm(term);
            }

            // Map properties from DTO to the entity
            mapper.updateCourseFromDto(dto, course);
            coursesToSave.add(course);
        }

        // Save all changes in a single batch operation and return the result
        List<Course> savedCourses = courseDao.saveAll(coursesToSave);
        return mapper.toCourseResponseDtoList(savedCourses);
    }

    /**
     * Helper method to validate the entire list for duplicates and invalid data.
     */
    private void validateCourseList(List<CourseResponseDTO> courseDTOs) {
        Set<String> seenCodes = new HashSet<>();
        Set<String> seenNames = new HashSet<>();

        for (CourseResponseDTO courseDTO : courseDTOs) {
            if (courseDTO.getCourseCode() == null || courseDTO.getCourseCode().trim().isEmpty()) {
                throw new ValidationException("Course code cannot be empty.");
            }

            if (courseDTO.getName() == null || courseDTO.getName().trim().isEmpty()) {
                throw new ValidationException("Course name cannot be empty.");
            }

            if (courseDTO.getCredit() == null || courseDTO.getCredit() <= 0) {
                throw new ValidationException("Invalid course credit for: " + courseDTO.getName());
            }

            if (!seenCodes.add(courseDTO.getCourseCode().trim())) {
                throw new ValidationException("Duplicate course code found in list: " + courseDTO.getCourseCode());
            }

            if (!seenNames.add(courseDTO.getName().trim())) {
                throw new ValidationException("Duplicate course name found in list: " + courseDTO.getName());
            }
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        String userUid = SecurityUtil.getAuthenticatedUid();

        Course courseToDelete = courseDao.findById(courseId);

        // Delete child entities first if not using cascade delete in JPA
        topicDao.deleteByCourse(courseToDelete);
        assignmentDao.deleteByCourse(courseToDelete);
        examDao.deleteByCourse(courseToDelete);

        courseDao.delete(courseToDelete);
    }

    @Override
    @Transactional
    public CourseResponseDTO updateCourseDetails(CourseResponseDTO details) {
        Course course = courseDao.findById(details.getCourseId());

        String userUid = SecurityUtil.getAuthenticatedUid();
        if (!course.getTerm().getUser().getUid().equals(userUid)) {
            throw new SecurityException("Unauthorized: Cannot update another user's course.");
        }

        Map<String, Topic> topicMap = saveTopics(details.getTopics(), course);
        saveExams(details.getExams(), course);
        saveAssignments(details.getAssignments(), course, topicMap);
        courseDao.flush();

        // Reload the course to ensure all collections are fresh before mapping to DTO
        Course updatedCourse = courseDao.findById(details.getCourseId());
        updatedCourse.getTopics().size();       // Trigger lazy loading
        updatedCourse.getAssignments().size();  // Trigger lazy loading
        updatedCourse.getExams().size();        // Trigger lazy loading

        return mapper.toCourseResponseDto(updatedCourse);
    }


    // ===================================================================================
    // === HELPER METHODS WITH DEBUG LOGGING ===
    // ===================================================================================

    public Map<String, Topic> saveTopics(List<TopicDTO> topicDTOs, Course course) {
        if (topicDTOs == null || topicDTOs.isEmpty()) {
            throw new ValidationException("At least one topic is required.");
        }

        Set<String> topicNames = new HashSet<>();
        for (TopicDTO dto : topicDTOs) {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new ValidationException("Topic name cannot be empty.");
            }
            if (!topicNames.add(dto.getName().trim())) {
                throw new ValidationException("Duplicated topic name: " + dto.getName().trim() + ".");
            }
        }

        Map<String, Topic> existingTopics = topicDao.findByCourse(course).stream()
                .collect(Collectors.toMap(Topic::getId, topic -> topic));

        Set<String> incomingTopicIds = new HashSet<>();
        Map<String, Topic> processedTopics = new HashMap<>();

        for (TopicDTO dto : topicDTOs) {
            // --- Validation ---
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                throw new ValidationException("Topic ID must not be null or empty");
            }
            if (dto.getEstimatedStudyTime() < 0) {
                throw new ValidationException("Invalid topic estimated study time.");
            }

            incomingTopicIds.add(dto.getId());

            Topic topic;
            if (existingTopics.containsKey(dto.getId())) {
                topic = existingTopics.get(dto.getId());

                // --- Ownership Check ---
                if (!Objects.equals(topic.getCourse().getTerm().getUser().getUid(), course.getTerm().getUser().getUid())) {
                    throw new SecurityException("Cannot modify topic not owned by the current user.");
                }

                mapper.updateTopicFromDto(dto, topic);
            } else {
                topic = mapper.toTopic(dto);
                topic.setCourse(course);
            }

            Topic savedTopic = topicDao.save(topic);
            processedTopics.put(savedTopic.getId(), savedTopic);
        }

        // --- Delete obsolete topics ---
        for (Topic existingTopic : existingTopics.values()) {
            if (!incomingTopicIds.contains(existingTopic.getId())) {
                topicDao.deleteById(existingTopic.getId());
            }
        }

        return processedTopics;
    }

    public void saveExams(List<ExamDTO> examDTOs, Course course) {
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

        LocalDate termStart = course.getTerm().getStartDate();
        LocalDate termEnd = course.getTerm().getEndDate();

        for (ExamDTO dto : examDTOs) {
            if (dto.getDate().isBefore(termStart) || dto.getDate().isAfter(termEnd)) {
                throw new ValidationException("Exam date must be within the term date range.");
            }
            if (dto.getEndTime().isBefore(dto.getStartTime())) {
                throw new ValidationException("Exam end time must be after start time.");
            }

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

    public void saveAssignments(List<AssignmentDTO> assignmentDTOs, Course course, Map<String, Topic> topicMap) {
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

        LocalDate termStart = course.getTerm().getStartDate();
        LocalDate termEnd = course.getTerm().getEndDate();

        for (AssignmentDTO dto : assignmentDTOs) {
            if (dto.getDueDate().isBefore(termStart) || dto.getDueDate().isAfter(termEnd)) {
                throw new ValidationException("Assignment due date must be within the term date range.");
            }

            // STC-08-TC-07: Test with invalid estimated time
            if (dto.getEstimatedTime() < 0) {
                throw new ValidationException("Invalid assignment estimated time.");
            }

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

    @Override
    @Transactional
    public List<AvailabilityDTO> saveAvailabilities(List<AvailabilityRequestDTO> availabilityDTOs) {
        if (availabilityDTOs == null) {
            throw new ValidationException("Availability list cannot be null.");
        }

        User user = fetchUser();

        // --- Pre-emptive Validation Block ---
        if (!availabilityDTOs.isEmpty()) {
            // Fetch term once for date range validation
            TermResponseDTO term = getCurrentTerm();
            LocalDate termStart = term.getStartDate();
            LocalDate termEnd = term.getEndDate();
            LocalDate today = LocalDate.now();

            Set<AvailabilityRequestDTO> uniqueSlots = new HashSet<>(availabilityDTOs);
            if (uniqueSlots.size() < availabilityDTOs.size()) {
                throw new ValidationException("Duplicate availability entry.");
            }

            for (AvailabilityRequestDTO dto : availabilityDTOs) {
                if (dto.getDate() == null) throw new ValidationException("Date is required.");
                if (dto.getStartTime() == null) throw new ValidationException("Start time cannot be empty.");
                if (dto.getEndTime() == null) throw new ValidationException("End time cannot be empty.");
                if (dto.getStartTime().isAfter(dto.getEndTime())) {
                    throw new ValidationException("Start time must be before end time.");
                }
                if (dto.getDate().isBefore(today)) {
                    throw new ValidationException("Cannot add availability for past date.");
                }
                if (dto.getDate().isBefore(termStart) || dto.getDate().isAfter(termEnd)) {
                    throw new ValidationException("Availability must be within the term date range.");
                }

                long durationHours = java.time.Duration.between(dto.getStartTime(), dto.getEndTime()).toHours();
                if (durationHours > MAX_AVAILABILITY_DURATION_HOURS) {
                    throw new ValidationException("Availability duration exceeds maximum allowed time per session.");
                }
            }

            List<AvailabilityRequestDTO> sortedSlots = availabilityDTOs.stream()
                    .sorted(Comparator.comparing(AvailabilityRequestDTO::getDate)
                            .thenComparing(AvailabilityRequestDTO::getStartTime))
                    .toList();

            for (int i = 1; i < sortedSlots.size(); i++) {
                AvailabilityRequestDTO prev = sortedSlots.get(i - 1);
                AvailabilityRequestDTO current = sortedSlots.get(i);
                // Check for overlap only if they are on the same day
                if (prev.getDate().equals(current.getDate())) {
                    if (current.getStartTime().isBefore(prev.getEndTime())) {
                        throw new ValidationException("Time slot overlaps with an existing availability on the same day.");
                    }
                }
            }
        }
        // --- End of Validation Block ---

        // Clear existing availabilities for the user to support "overwrite" behavior
        availabilityDao.deleteByUser(user);

        List<AvailabilityDTO> savedDTOs = new ArrayList<>();

        for (AvailabilityRequestDTO dto : availabilityDTOs) {
            Availability availability = Availability.builder()
                    .user(user)
                    .date(dto.getDate())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .build();

            Availability saved = availabilityDao.save(availability);

            // Map back to DTO (assuming a mapper is available)
            AvailabilityDTO savedDTO = AvailabilityDTO.builder()
                    .id(saved.getId())
                    .date(saved.getDate())
                    .startTime(saved.getStartTime())
                    .endTime(saved.getEndTime())
                    .build();

            savedDTOs.add(savedDTO);
        }

        return savedDTOs;
    }

    @Override
    @Transactional
    public List<AvailabilityDTO> getAvailabilities() {
        User user = fetchUser();
        List<Availability> availabilities = availabilityDao.findByUser(user);
        return availabilities.stream()
                .map(a -> AvailabilityDTO.builder()
                        .id(a.getId())
                        .date(a.getDate())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudySetupDTO getStudySetup() {
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