package senior.project.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.dto.StudyPreferenceDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePlanRequestDTO {

    private String userUid;
    private StudyPreferenceDTO studyPreferences;
    private StudySetupDTO studySetup;
}
