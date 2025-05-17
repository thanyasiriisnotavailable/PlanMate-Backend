package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "study_preferences")
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class StudyPreference {

    @Id
    private String userUid;

    private Integer minSessionDuration;
    private Integer maxSessionDuration;

    @Column(columnDefinition = "json")
    private String preferredStudyTimes;

    @Column(columnDefinition = "json")
    private String revisionFrequency;

    @Column(columnDefinition = "json")
    private String breakDurations;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_uid")
    private User user;
}
