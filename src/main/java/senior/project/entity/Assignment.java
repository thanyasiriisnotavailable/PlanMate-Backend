package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.enums.ExamType;

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
    private String id;

    private String name;
    private LocalDate dueDate;
    private String dueTime;
    private Long estimatedTime;
    private ExamType examType;
    private Boolean completed;

    @ManyToOne
    private Course course;

    @ManyToMany
    private List<Topic> associatedTopics;
}