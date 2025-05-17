package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.StudyPreference;

public interface StudyPreferenceRepository extends JpaRepository<StudyPreference, String> {
}
