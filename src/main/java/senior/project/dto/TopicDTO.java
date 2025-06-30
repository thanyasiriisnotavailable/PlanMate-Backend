package senior.project.dto;

import lombok.*;
import senior.project.enums.ExamType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {
    private String id;
    private String name;
    private Long difficulty;
    private Long confidence;
    private Long estimatedStudyTime;
    private ExamType examType;
}
