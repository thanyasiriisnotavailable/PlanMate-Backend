package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "study_preferences")
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class StudyPreference {

    @Id
    @Column(name = "user_uid")
    private String userUid;

    private Integer minSessionDuration;
    private Integer maxSessionDuration;

    @ElementCollection
    private List<String> preferredStudyTimes;

    private String revisionFrequency;
    private Integer breakDurations;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_uid")
    private User user;
}