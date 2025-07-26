package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;

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

    private LocalDateTime focusStart;

    private LocalDateTime focusEnd;

    private Long elapsedSeconds; // useful for analysis

    private FocusStatus status;
}