package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    @Column(name = "term_id")
    private Long termId;

    private String name;  // e.g., "Spring 2025"

    @Column(name = "start_date")
    private LocalDate startDate; // e.g., "2025-01-10"

    @Column(name = "end_date")
    private LocalDate endDate;   // e.g., "2025-05-20"

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Course> courses;
}