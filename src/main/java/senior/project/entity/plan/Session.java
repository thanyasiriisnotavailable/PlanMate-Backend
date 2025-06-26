package senior.project.entity.plan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.Course;
import senior.project.entity.Topic;

import java.util.Date;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String type; // "study", "review", "assignment"
    private Date date;
    private String start;
    private String end;
    private Long durationMinutes;

    private Boolean completed = false;

    @ManyToOne
    private Schedule schedule;

    @ManyToOne
    private Course course;

    @ManyToOne(optional = true)
    private Topic topic;
}
