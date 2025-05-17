package senior.project.dao;

import senior.project.entity.StudyPreference;
import java.util.Optional;

public interface StudyPreferenceDao {
    StudyPreference savePreference(StudyPreference preference);
    Optional<StudyPreference> findByUserUid(String userUid);
}
