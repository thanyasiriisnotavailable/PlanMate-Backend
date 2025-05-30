package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetupDTO {
    private String userUid;
    private TermDTO term;
    private List<AvailabilityDTO> availabilities;
}