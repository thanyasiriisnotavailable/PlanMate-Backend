package senior.project.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private String title;
    private LocalDate dueDate;
    private String dueTime;
    private Long estimatedTime;
    private Integer associatedTopicIndex; // Optional index reference to topic list
}
