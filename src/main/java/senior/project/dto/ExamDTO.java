package senior.project.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamDTO {
    private String type; // e.g., "mid", "final"
    private LocalDate date; // e.g., "2025-02-25"
    private String time; // e.g., "09:00–11:00"
}
