package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPreferenceDTO {
    private String userUid;
    private Integer minSessionDuration;
    private Integer maxSessionDuration;
    private List<String> preferredStudyTimes;
    private String revisionFrequency;
    private Integer breakDurations;
}
