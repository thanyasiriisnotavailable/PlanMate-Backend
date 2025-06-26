package senior.project.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermResponseDTO {
    private Long termId;
    private String name; // e.g., "Spring 2025"
    private LocalDate startDate; // e.g., "2025-01-10"
    private LocalDate endDate;   // e.g., "2025-05-20"
    private List<CourseResponseDTO> courses;
}
