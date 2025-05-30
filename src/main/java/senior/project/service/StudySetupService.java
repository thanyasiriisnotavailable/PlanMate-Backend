package senior.project.service;

import senior.project.dto.StudySetupDTO;

public interface StudySetupService {
    void processStudySetup(String userUid, StudySetupDTO dto);
}