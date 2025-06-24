package senior.project.dto;

import lombok.*;
import senior.project.entity.CourseId;
import senior.project.enums.ExamType;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private String id;
    private String name;
    private LocalDate dueDate;
    private String dueTime;
    private Long estimatedTime;
    private ExamType examType;
    private Boolean completed;
    private List<String> associatedTopicIds;

    private CourseId courseId;
}
