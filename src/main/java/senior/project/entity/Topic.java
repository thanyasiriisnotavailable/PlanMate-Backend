package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Long difficulty;
    private Long confidence;
    private Long estimatedStudyTime;

    @ManyToOne
    private Course course;
}