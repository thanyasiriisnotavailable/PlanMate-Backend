package senior.project.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import senior.project.dto.*;
import senior.project.entity.*;

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

    Term toTerm(TermRequestDTO dto);

    default Term toTerm(TermRequestDTO dto, User user) {
        Term term = toTerm(dto);
        term.setUser(user);
        return term;
    }

    // === Course Mapping ===

    @Mapping(target = "courseCode", source = "courseId.courseCode")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "credit", source = "credit")
    CourseBaseDTO toCourseBaseDto(Course course);

    @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "courseCode", source = "courseId.courseCode")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "credit", source = "credit")
    @Mapping(target = "topics", source = "topics")
    @Mapping(target = "assignments", source = "assignments")
    @Mapping(target = "exams", source = "exams")
    CourseResponseDTO toCourseResponseDto(Course course);

    List<CourseResponseDTO> toCourseResponseDtoList(List<Course> courses);

    @Mapping(target = "courseId", ignore = true) // courseId is part of the path, so don't update directly
    @Mapping(target = "term", ignore = true) // term is set separately
    @Mapping(target = "topics", ignore = true) // topics, assignments, exams are handled in service
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "exams", ignore = true)
    void updateCourseFromDto(CourseResponseDTO dto, @MappingTarget Course course);

    // Default mapper for creating entity from base DTO
    default Course toCourse(CourseBaseDTO dto, Term term) {
        if (dto == null || term == null || term.getTermId() == null) return null;

        return Course.builder()
                .courseId(new CourseId(term.getTermId(), dto.getCourseCode()))
                .name(dto.getName())
                .credit(dto.getCredit())
                .term(term)
                .build();
    }

    // Topic
    @Mapping(target = "id", source = "id")
    @Mapping(target = "course.courseId", source = "courseId")
    Topic toTopic(TopicDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateTopicFromDto(TopicDTO dto, @MappingTarget Topic topic);

    @Mapping(target = "courseId", source = "course.courseId")
    TopicDTO toTopicDto(Topic topic);

    List<TopicDTO> toTopicDtoList(List<Topic> list);

    // Assignment
    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "course.courseId", source = "dto.courseId")
    Assignment toAssignment(AssignmentDTO dto, @MappingTarget Assignment assignment);

    @Mapping(target = "courseId", source = "course.courseId")
    @Mapping(target = "associatedTopicIds", expression = "java(assignment.getAssociatedTopics() != null ? assignment.getAssociatedTopics().stream().map(Topic::getId).toList() : null)")
    AssignmentDTO toAssignmentDto(Assignment assignment);

    List<AssignmentDTO> toAssignmentDtoList(List<Assignment> list);

    // Exam
    @Mapping(target = "id", source = "id")
    @Mapping(target = "course.courseId", source = "courseId")
    Exam toExam(ExamDTO dto);

    @Mapping(target = "courseId", source = "course.courseId")
    ExamDTO toExamDto(Exam exam);

    void updateExamFromDto(ExamDTO dto, @MappingTarget Exam exam);
    List<ExamDTO> toExamDtoList(List<Exam> list);

    // Availability
    AvailabilityDTO toAvailabilityDto(Availability availability);
    Availability toAvailability(AvailabilityDTO dto);
    List<AvailabilityDTO> toAvailabilityDtoList(List<Availability> list);
}
