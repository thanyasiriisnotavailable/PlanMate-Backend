package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamDTO {
    private Long id;
    private String type;
    private String date;
    private String time;
    private Long courseId;
}
