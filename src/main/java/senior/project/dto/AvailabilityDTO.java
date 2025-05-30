package senior.project.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityDTO {
    private LocalDate date;
    private String startTime;
    private String endTime;
}
