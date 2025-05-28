package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {
    private Long id;
    private String name;
    private Long difficulty;
    private Long confidence;
    private Long estimatedStudyTime;
    private Long courseId;
}
