package senior.project.entity.plan;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.*;
import senior.project.entity.plan.Schedule;
import senior.project.enums.SessionType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    @Id
    private String sessionId;
    private LocalDate date;
    private String start;
    private String end;
    private Long duration;
    private Boolean isCompleted;
    private Boolean isScheduled;
    private Integer sessionNumber;
    private Integer totalSessionsInGroup;

    @Enumerated(EnumType.STRING)
    private SessionType type; // "study", "review", "assignment"

    @ManyToOne
    @JsonBackReference
    private Schedule schedule;

    @ManyToOne
    private Course course;

    @ManyToOne
    private Topic topic;

    @ManyToOne
    private Assignment assignment;

    @OneToMany(mappedBy = "session")
    private List<FocusSession> focusSessions;
}