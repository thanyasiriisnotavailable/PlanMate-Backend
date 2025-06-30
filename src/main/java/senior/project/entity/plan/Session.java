package senior.project.entity.plan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.Assignment;
import senior.project.entity.Course;
import senior.project.entity.Exam;
import senior.project.entity.Topic;
import senior.project.entity.plan.Schedule;
import senior.project.enums.SessionType;

import java.time.LocalDate;
import java.time.LocalTime;

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

    @Enumerated(EnumType.STRING)
    private SessionType type; // "study", "review", "assignment"

    @ManyToOne
    private Schedule schedule;

    @ManyToOne
    private Course course;

    @ManyToOne
    private Topic topic;

    @ManyToOne
    private Assignment assignment;
}