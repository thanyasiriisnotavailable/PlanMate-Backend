package senior.project.service;

import senior.project.entity.StudyPreference;
import java.util.Optional;

public interface StudyPreferenceService {
    StudyPreference savePreference(StudyPreference preference);
    Optional<StudyPreference> getPreference(String userUid);
}
