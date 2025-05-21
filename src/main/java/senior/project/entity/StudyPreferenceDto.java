package senior.project.entity;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPreferenceDto {
    private String userUid;
    private Integer minSessionDuration;
    private Integer maxSessionDuration;
    private List<String> preferredStudyTimes;
    private String revisionFrequency;
    private String breakDurations;
}
