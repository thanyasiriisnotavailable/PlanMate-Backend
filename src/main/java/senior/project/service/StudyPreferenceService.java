package senior.project.service;

import senior.project.dto.StudyPreferenceDTO;

import java.util.Optional;

public interface StudyPreferenceService {
    StudyPreferenceDTO getPreference();
    StudyPreferenceDTO saveOrUpdate(StudyPreferenceDTO dto);
}