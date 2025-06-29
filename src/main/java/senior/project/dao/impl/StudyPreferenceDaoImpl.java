package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.StudyPreferenceDao;
import senior.project.entity.StudyPreference;
import senior.project.repository.StudyPreferenceRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudyPreferenceDaoImpl implements StudyPreferenceDao {

    private final StudyPreferenceRepository repository;

    @Override
    public StudyPreference savePreference(StudyPreference preference) {
        return repository.save(preference);
    }

    @Override
    public StudyPreference findByUserUid(String userUid) {
        return repository.findById(userUid).orElse(null);
    }
}