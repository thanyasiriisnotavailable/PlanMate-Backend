package senior.project.util;

import org.mapstruct.*;
import senior.project.dto.*;
import senior.project.entity.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppMapper {

    // --- Study Preference ---
    @Mapping(source = "userUid", target = "userUid")
    StudyPreferenceDTO toStudyPreferenceDto(StudyPreference entity);

    @Mapping(target = "userUid", source = "user.uid")
    StudyPreference toStudyPreference(StudyPreferenceDTO dto, User user);

    // --- Term ---
    @Mapping(source = "user.uid", target = "userUid")
    TermDTO toTermDto(Term term);

    @Mapping(target = "user.uid", source = "userUid")
    Term toTerm(TermDTO dto);

    List<TermDTO> toTermDto(List<Term> terms);
    List<Term> toTerms(List<TermDTO> dtos);

    // --- Course ---
    @Mapping(source = "term.id", target = "termId")
    CourseDTO toCourseDto(Course course);

    @Mapping(target = "term.id", source = "termId")
    Course toCourse(CourseDTO dto);

    List<CourseDTO> toCourseDtos(List<Course> courses);
    List<Course> toCourses(List<CourseDTO> dtos);

    // --- Topic ---
    @Mapping(source = "course.id", target = "courseId")
    TopicDTO toTopicDto(Topic topic);

    @Mapping(target = "course.id", source = "courseId")
    Topic toTopic(TopicDTO dto);

    List<TopicDTO> toTopicDtos(List<Topic> topics);
    List<Topic> toTopics(List<TopicDTO> dtos);

    // --- Assignment ---
    @Mapping(source = "course.id", target = "courseId")
    AssignmentDTO toAssignmentDto(Assignment assignment);

    @Mapping(target = "course.id", source = "courseId")
    Assignment toAssignment(AssignmentDTO dto);

    List<AssignmentDTO> toAssignmentDtos(List<Assignment> assignments);
    List<Assignment> toAssignments(List<AssignmentDTO> dtos);

    // --- Exam ---
    @Mapping(source = "course.id", target = "courseId")
    ExamDTO toExamDto(Exam exam);

    @Mapping(target = "course.id", source = "courseId")
    Exam toExam(ExamDTO dto);

    List<ExamDTO> toExamDtos(List<Exam> exams);
    List<Exam> toExams(List<ExamDTO> dtos);

    // --- Availability ---
    default AvailabilityDTO toAvailabilityDto(String userUid, List<Availability> entities) {
        List<AvailabilityDTO.AvailabilitySlot> slots = entities.stream().map(entity ->
                AvailabilityDTO.AvailabilitySlot.builder()
                        .dayOfWeek(entity.getDayOfWeek())
                        .startTime(entity.getStartTime())
                        .endTime(entity.getEndTime())
                        .build()
        ).toList();

        return AvailabilityDTO.builder()
                .userUid(userUid)
                .slots(slots)
                .build();
    }

    default List<Availability> toAvailabilityEntities(AvailabilityDTO dto, User user) {
        return dto.getSlots().stream().map(slot ->
                Availability.builder()
                        .dayOfWeek(slot.getDayOfWeek())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .user(user)
                        .build()
        ).toList();
    }
}