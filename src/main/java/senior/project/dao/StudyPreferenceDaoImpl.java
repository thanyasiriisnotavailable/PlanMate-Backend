package senior.project.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import senior.project.entity.StudyPreference;
import senior.project.repository.StudyPreferenceRepository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("db")
public class StudyPreferenceDaoImpl implements StudyPreferenceDao {

    @Autowired
    private StudyPreferenceRepository repository;

    @Override
    public StudyPreference savePreference(StudyPreference preference) {
        return repository.save(preference);
    }

    @Override
    public Optional<StudyPreference> findByUserUid(String userUid) {
        return repository.findById(userUid);
    }
}
