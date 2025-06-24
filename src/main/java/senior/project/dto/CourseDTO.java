package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private String courseCode;
    private String name;
    private Long credit;
    private List<TopicDTO> topics;
    private List<AssignmentDTO> assignments;
    private List<ExamDTO> exams;
}
