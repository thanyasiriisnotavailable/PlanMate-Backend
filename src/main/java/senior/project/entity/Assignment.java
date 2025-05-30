package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDate dueDate;
    private String dueTime;
    private Long estimatedTime;

    @ManyToOne
    private Course course;

    @ManyToOne(optional = true)
    private Topic associatedTopic; // Optional link
}