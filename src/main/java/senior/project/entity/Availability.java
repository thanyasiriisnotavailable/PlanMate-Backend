package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    private LocalDate date;
    private String startTime; // e.g., "14:00"
    private String endTime;   // e.g., "16:00"

    @ManyToOne
    private User user;
}