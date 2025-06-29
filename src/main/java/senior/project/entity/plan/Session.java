package senior.project.entity.plan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.Course;
import senior.project.entity.Exam;
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

    private String topic;
    private String title;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Long duration;
    private Boolean completed = false;

    @Enumerated(EnumType.STRING)
    private SessionType type; // "study", "review", "assignment"

    @ManyToOne
    private Schedule schedule;

    @ManyToOne
    private Course course;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;
}