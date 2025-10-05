package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.StudyGroup;

import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    boolean existsByJoinCode(String code);
    Optional<StudyGroup> findByJoinCode(String joinCode);
}
