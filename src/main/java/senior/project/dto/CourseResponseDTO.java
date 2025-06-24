package senior.project.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import senior.project.entity.CourseId;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CourseResponseDTO extends CourseBaseDTO {
    private CourseId courseId;
    private List<TopicDTO> topics;
    private List<AssignmentDTO> assignments;
    private List<ExamDTO> exams;
}
