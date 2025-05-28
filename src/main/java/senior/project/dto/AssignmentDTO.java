package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private Long id;
    private String title;
    private String dueDate;
    private String dueTime;
    private Long estimatedHours;
    private Long courseId;
}
