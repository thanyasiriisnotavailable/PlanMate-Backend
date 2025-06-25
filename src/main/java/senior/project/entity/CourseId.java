package senior.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CourseId implements Serializable {

    private Long termId;

    @Column(name = "course_code")
    private String courseCode;

    public CourseId(CourseId courseId) {
        this.termId = courseId.termId;
        this.courseCode = courseId.courseCode;
    }
}
