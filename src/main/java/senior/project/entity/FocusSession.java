package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;
import senior.project.enums.SessionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "focus_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Session session;

    @ManyToOne
    private Course course;

    @ManyToOne
    private Topic topic;

    @ManyToOne
    private Assignment assignment;

    private LocalDateTime focusStart;

    private LocalDateTime focusEnd;

    private Long elapsedSeconds; // useful for analysis

    private Long plannedDuration;

    @Enumerated(EnumType.STRING)
    private FocusStatus status;

    @Enumerated(EnumType.STRING)
    private SessionType sessionType;
}