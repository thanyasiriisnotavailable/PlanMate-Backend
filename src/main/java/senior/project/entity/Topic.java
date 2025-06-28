package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.enums.ExamType;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Topic {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name;
    private Long difficulty;
    private Long confidence;
    private Long estimatedStudyTime;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @ManyToOne
    private Course course;
}