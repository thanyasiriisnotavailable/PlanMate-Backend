package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private CourseId courseId;

    private String name;
    private Long credit;

    @MapsId("termId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "term_id")
    @ToString.Exclude
    private Term term;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Topic> topics;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Exam> exams;
}

