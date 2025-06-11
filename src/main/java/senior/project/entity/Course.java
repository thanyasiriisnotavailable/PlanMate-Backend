package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    private String id;

    private String name;

    private Long credit;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Term term;

    @OneToMany(mappedBy = "course")
    private List<Topic> topics;

    @OneToMany(mappedBy = "course")
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "course")
    private List<Exam> exams;
}