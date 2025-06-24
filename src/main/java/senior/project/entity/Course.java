package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @EmbeddedId
    private CourseId courseId;

    private String name;
    private Long credit;

    @MapsId("termId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "term_id")
    private Term term;

    @OneToMany(mappedBy = "course")
    private List<Topic> topics;

    @OneToMany(mappedBy = "course")
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "course")
    private List<Exam> exams;
}

