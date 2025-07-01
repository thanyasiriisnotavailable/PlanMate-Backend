package senior.project.util;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import senior.project.dto.*;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.ScheduleViewDTO;
import senior.project.dto.plan.SessionDTO;
import senior.project.dto.plan.SessionViewDTO;
import senior.project.entity.*;
import senior.project.entity.plan.Schedule;
import senior.project.entity.plan.Session;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DTOMapper {
    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    // === Study Preference Mapping ===
    StudyPreferenceDTO toStudyPreferenceDto(StudyPreference preference);

    StudyPreference toStudyPreference(StudyPreferenceDTO dto);

    default StudyPreference toStudyPreference(StudyPreferenceDTO dto, User user) {
        StudyPreference pref = toStudyPreference(dto);
        pref.setUser(user);
        return pref;
    }

    // === Term Mapping ===
    TermResponseDTO toTermDto(Term term);

    @Mapping(target = "user", ignore = true)
    Term termResponseToTerm(TermRequestDTO dto);

    Term toTerm(TermRequestDTO dto);

    default Term toTerm(TermRequestDTO dto, User user) {
        Term term = termResponseToTerm(dto);
        term.setUser(user);
        return term;
    }

    // === Course Mapping ===
    @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "courseCode", source = "courseCode")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "credit", source = "credit")
    @Mapping(target = "topics", source = "topics")
    @Mapping(target = "assignments", source = "assignments")
    @Mapping(target = "exams", source = "exams")
    CourseResponseDTO toCourseResponseDto(Course course);

    List<CourseResponseDTO> toCourseResponseDtoList(List<Course> courses);

    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "term", ignore = true)
    @Mapping(target = "topics", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "exams", ignore = true)
    void updateCourseFromDto(CourseResponseDTO dto, @MappingTarget Course course);

    default Course toCourse(CourseBaseDTO dto, Term term) {
        if (dto == null || term == null || term.getTermId() == null) return null;

        return Course.builder()
                .courseCode(dto.getCourseCode())
                .name(dto.getName())
                .credit(dto.getCredit())
                .term(term)
                .build();
    }

    @AfterMapping
    default void establishTermCourseRelationship(TermResponseDTO dto, @MappingTarget Term term) {
        if (term.getCourses() != null) {
            term.getCourses().forEach(course -> course.setTerm(term));
        }
    }

    // Topic
    @Mapping(target = "id", source = "id")
    Topic toTopic(TopicDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateTopicFromDto(TopicDTO dto, @MappingTarget Topic topic);

    List<TopicDTO> toTopicDtoList(List<Topic> list);

    // Assignment
    @Mapping(target = "id", source = "dto.id")
    Assignment toAssignment(AssignmentDTO dto, @MappingTarget Assignment assignment);

    @Mapping(target = "associatedTopicIds", expression = "java(assignment.getAssociatedTopics() != null ? assignment.getAssociatedTopics().stream().map(Topic::getId).toList() : null)")
    AssignmentDTO toAssignmentDto(Assignment assignment);

    List<AssignmentDTO> toAssignmentDtoList(List<Assignment> list);

    // Exam
    @Mapping(target = "id", source = "id")
    Exam toExam(ExamDTO dto);

    ExamDTO toExamDto(Exam exam);

    void updateExamFromDto(ExamDTO dto, @MappingTarget Exam exam);
    List<ExamDTO> toExamDtoList(List<Exam> list);

    // Availability
    AvailabilityDTO toAvailabilityDto(Availability availability);
    Availability toAvailability(AvailabilityDTO dto);
    List<AvailabilityDTO> toAvailabilityDtoList(List<Availability> list);

    // Schedule and Session Mappings
    default ScheduleDTO toScheduleDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        List<SessionDTO> studyPlan = new ArrayList<>();
        List<SessionDTO> unscheduledPlan = new ArrayList<>();

        if (schedule.getSessions() != null) {
            for (Session session : schedule.getSessions()) {
                SessionDTO sessionDTO = toSessionDto(session); // Use the helper
                if (session.getIsScheduled() != null && session.getIsScheduled()) {
                    studyPlan.add(sessionDTO);
                } else {
                    unscheduledPlan.add(sessionDTO);
                }
            }
        }

        return ScheduleDTO.builder()
                .id(String.valueOf(schedule.getId()))
                .generatedAt(schedule.getGeneratedAt().toString())
                .examType(schedule.getExamType())
                .termId(schedule.getTerm() != null ? schedule.getTerm().getTermId() : null)
                .studyPlan(studyPlan)
                .unscheduledPlan(unscheduledPlan)
                .build();
    }

    Session toSession(SessionDTO sDto);

    @Mapping(target = "isScheduled", source = "isScheduled") // Ensure boolean is mapped
    @Mapping(source = "course.courseId", target = "courseId")
    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "assignment.id", target = "assignmentId")
    SessionDTO toSessionDto(Session session);

    Term toTerm(TermResponseDTO term, User user);

    @Mapping(target = "term", ignore = true)
    @Mapping(target = "topics", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "exams", ignore = true)
    Course responsetoCourse(CourseResponseDTO courseDTO);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "associatedTopics", ignore = true)
    Assignment dtoToAssignment(AssignmentDTO assignmentDTO);

    /**
     * Converts a Schedule entity to its view representation (ScheduleViewDTO).
     * This method provides a custom implementation using default interface methods.
     * @param schedule The Schedule entity to convert.
     * @return A ScheduleViewDTO containing lists of scheduled and unscheduled sessions.
     */
    default ScheduleViewDTO toScheduleViewDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        List<SessionViewDTO> scheduledSessions = new ArrayList<>();
        List<SessionViewDTO> unscheduledSessions = new ArrayList<>();

        // Ensure sessions are not null before iterating
        if (schedule.getSessions() != null) {
            for (Session session : schedule.getSessions()) {
                SessionViewDTO sessionViewDTO = toSessionViewDto(session);
                if (session.getIsScheduled() != null && session.getIsScheduled()) {
                    scheduledSessions.add(sessionViewDTO);
                } else {
                    unscheduledSessions.add(sessionViewDTO);
                }
            }
        }

        return ScheduleViewDTO.builder()
                .studyPlan(scheduledSessions)
                .unscheduledPlan(unscheduledSessions)
                .build();
    }

    /**
     * Converts a Session entity to its view representation (SessionViewDTO).
     * This method resolves entity IDs into human-readable names and codes.
     * @param session The Session entity to convert.
     * @return A SessionViewDTO with detailed information for display.
     */
    default SessionViewDTO toSessionViewDto(Session session) {
        if (session == null) {
            return null;
        }

        // Use a null-safe way to get related entity names/codes
        String courseCode = (session.getCourse() != null) ? session.getCourse().getCourseCode() : null;
        String topicName = (session.getTopic() != null) ? session.getTopic().getName() : null;
        String assignmentName = (session.getAssignment() != null) ? session.getAssignment().getName() : null;

        return SessionViewDTO.builder()
                .sessionId(String.valueOf(session.getSessionId())) // Assuming Session has a Long id
                .courseCode(courseCode)
                .topicName(topicName)
                .assignmentName(assignmentName)
                .date(session.getDate())
                .start(session.getStart())
                .end(session.getEnd())
                .duration(session.getDuration())
                .type(session.getType())
                .isScheduled(session.getIsScheduled())
                .isCompleted(session.getIsCompleted())
                .build();
    }
}