package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "availabilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_uid", referencedColumnName = "uid")
    private User user;

    private String dayOfWeek; // e.g., "Monday"
    private String startTime; // e.g., "14:00"
    private String endTime;   // e.g., "16:00"
}