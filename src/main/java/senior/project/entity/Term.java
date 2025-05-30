package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "terms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Term {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // e.g., "Spring 2025"
    private String startDate; // e.g., "2025-01-10"
    private String endDate;   // e.g., "2025-05-20"

    @ManyToOne
    @JoinColumn(name = "user_uid")
    private User user;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<Course> courses;
}