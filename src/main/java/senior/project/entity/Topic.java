package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.enums.ExamType;

@Entity
@Table(name = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {
    @Id
    private String id;

    private String name;
    private Long difficulty;
    private Long confidence;
    private Long estimatedStudyTime;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "term_id"),
            @JoinColumn(name = "course_code")
    })
    private Course course;
}