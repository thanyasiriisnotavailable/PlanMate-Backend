package senior.project.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CourseResponseDTO extends CourseBaseDTO {
    private Long courseId;
    private List<TopicDTO> topics;
    private List<AssignmentDTO> assignments;
    private List<ExamDTO> exams;
}
