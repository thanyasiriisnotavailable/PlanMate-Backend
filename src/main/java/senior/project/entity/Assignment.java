package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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

    private String name;
    private LocalDate dueDate;
    private String dueTime;
    private Long estimatedTime;

    @ManyToOne
    private Course course;

    @ManyToMany
    private List<Topic> associatedTopics;
}