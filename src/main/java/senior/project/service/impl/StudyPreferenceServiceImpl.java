package senior.project.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import senior.project.dao.StudyPreferenceDao;
import senior.project.entity.StudyPreference;
import senior.project.service.StudyPreferenceService;
import java.util.Optional;

@Service
public class StudyPreferenceServiceImpl implements StudyPreferenceService {

    @Autowired
    private StudyPreferenceDao studyPreferenceDao;

    @Override
    public StudyPreference savePreference(StudyPreference preference) {
        return studyPreferenceDao.savePreference(preference);
    }

    @Override
    public Optional<StudyPreference> getPreference(String userUid) {
        return studyPreferenceDao.findByUserUid(userUid);
    }
}
