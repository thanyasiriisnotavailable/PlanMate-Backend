package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetupResponseDTO {
    private String userUid;
    private TermResponseDTO term;
    private List<AvailabilityDTO> availabilities;
}