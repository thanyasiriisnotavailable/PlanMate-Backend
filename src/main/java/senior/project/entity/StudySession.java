package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;      // "2025-06-10"
    private String startTime; // "13:00"
    private String endTime;   // "14:30"

    private String sessionType; // "topic", "revision", "assignment"

    @ManyToOne
    @JoinColumn(name = "user_uid", referencedColumnName = "uid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = true)
    private String topicName;
}