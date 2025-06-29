package senior.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import senior.project.enums.ExamType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
    private String id;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")    
    private LocalDate dueDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime dueTime;

    private Long estimatedTime;
    private ExamType examType;
    private Boolean completed;
    private List<String> associatedTopicIds;

    private Long courseId;
}
