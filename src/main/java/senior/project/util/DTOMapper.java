package senior.project.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import senior.project.dto.*;
import senior.project.entity.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DTOMapper {
    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    // Study Preference
    StudyPreferenceDTO toStudyPreferenceDto(StudyPreference preference);

    StudyPreference toStudyPreference(StudyPreferenceDTO dto);

    default StudyPreference toStudyPreference(StudyPreferenceDTO dto, User user) {
        StudyPreference pref = toStudyPreference(dto);
        pref.setUser(user);
        return pref;
    }

    // Term
    TermResponseDTO toTermDto(Term term);

    Term toTerm(TermRequestDTO dto);

    default Term toTerm(TermRequestDTO dto, User user) {
        Term term = toTerm(dto);
        term.setUser(user);
        return term;
    }

    // CourseId mapping
    CourseIdDTO toCourseIdDto(CourseId id);

    CourseId toCourseId(CourseIdDTO dto);

    // Course with term ID
    default Course toCourse(CourseDTO dto, Term term) {
        if (dto == null || term == null || term.getTermId() == null) return null;

        Course course = new Course();
        course.setTerm(term); // set term before setting ID
        CourseId id = new CourseId(term.getTermId(), dto.getCourseCode());
        course.setCourseId(id);
        course.setName(dto.getName());
        course.setCredit(dto.getCredit());
        return course;
    }



    @Mapping(target = "courseCode", source = "courseId.courseCode")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "credit", source = "credit")
    CourseDTO toCourseDto(Course course);

    List<CourseDTO> toCourseDtoList(List<Course> list);

    // Topic
    @Mapping(target = "course.courseId", source = "courseId")
    Topic toTopic(TopicDTO dto);

    @Mapping(target = "courseId", source = "course.courseId")
    TopicDTO toTopicDto(Topic topic);

    List<TopicDTO> toTopicDtoList(List<Topic> list);

    // Assignment
    @Mapping(target = "course.courseId", source = "courseId")
    Assignment toAssignment(AssignmentDTO dto);

    @Mapping(target = "courseId", source = "course.courseId")
    @Mapping(target = "associatedTopicIds", expression = "java(assignment.getAssociatedTopics() != null ? assignment.getAssociatedTopics().stream().map(Topic::getId).toList() : null)")
    AssignmentDTO toAssignmentDto(Assignment assignment);

    List<AssignmentDTO> toAssignmentDtoList(List<Assignment> list);

    // Exam
    @Mapping(target = "course.courseId", source = "courseId")
    Exam toExam(ExamDTO dto);

    @Mapping(target = "courseId", source = "course.courseId")
    ExamDTO toExamDto(Exam exam);

    List<ExamDTO> toExamDtoList(List<Exam> list);

    // Availability
    AvailabilityDTO toAvailabilityDto(Availability availability);

    Availability toAvailability(AvailabilityDTO dto);

    List<AvailabilityDTO> toAvailabilityDtoList(List<Availability> list);
}
