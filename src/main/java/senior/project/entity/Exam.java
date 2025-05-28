package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "mid", "final", etc.

    private String date; // Could also use LocalDate
    private String time;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
