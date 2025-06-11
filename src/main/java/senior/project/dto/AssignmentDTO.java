package senior.project.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private String name;
    private LocalDate dueDate;
    private String dueTime;
    private Long estimatedTime;
    private List<String> associatedTopicTitles;
}
