package senior.project.dto.plan;

import lombok.*;
import senior.project.dto.AvailabilityDTO;
import senior.project.dto.TermResponseDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetupDTO {
    private String userUid;
    private TermResponseDTO term;
    private List<AvailabilityDTO> availabilities;
}