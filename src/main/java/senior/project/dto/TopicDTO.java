package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {
    private String title;
    private Long difficulty;
    private Long confidence;
    private Long estimatedStudyTime;
}
