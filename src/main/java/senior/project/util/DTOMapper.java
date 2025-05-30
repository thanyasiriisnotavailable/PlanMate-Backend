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
    StudyPreference toStudyPreference(StudyPreferenceDTO dto, User user);

    // Term
    TermDTO toTermDto(Term term);
    Term toTerm(TermDTO dto);
    Term toTerm(TermDTO dto, User user);

    // Course
    CourseDTO toCourseDto(Course course);

    @Mapping(target = "id", source = "id")
    Course toCourse(CourseDTO dto);

    List<CourseDTO> toCourseDtoList(List<Course> list);

    // Topic
    TopicDTO toTopicDto(Topic topic);
    Topic toTopic(TopicDTO dto);
    List<TopicDTO> toTopicDtoList(List<Topic> list);

    // Assignment
    AssignmentDTO toAssignmentDto(Assignment assignment);
    Assignment toAssignment(AssignmentDTO dto);
    List<AssignmentDTO> toAssignmentDtoList(List<Assignment> list);

    // Exam
    ExamDTO toExamDto(Exam exam);
    Exam toExam(ExamDTO dto);
    List<ExamDTO> toExamDtoList(List<Exam> list);

    // Availability
    AvailabilityDTO toAvailabilityDto(Availability availability);
    Availability toAvailability(AvailabilityDTO dto);
    List<AvailabilityDTO> toAvailabilityDtoList(List<Availability> list);
}