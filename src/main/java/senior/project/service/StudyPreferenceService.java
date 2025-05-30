package senior.project.service;

import senior.project.dto.StudyPreferenceDTO;

import java.util.Optional;

public interface StudyPreferenceService {
    Optional<StudyPreferenceDTO> getPreference(String userUid);
    StudyPreferenceDTO saveOrUpdate(String userUid, StudyPreferenceDTO dto);
}