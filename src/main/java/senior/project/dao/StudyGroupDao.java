package senior.project.dao;

import senior.project.entity.StudyGroup;

import java.util.Optional;

public interface StudyGroupDao {
    StudyGroup save(StudyGroup studyGroup);
    boolean existsByJoinCode(String code);
    Optional<StudyGroup> findByJoinCode(String joinCode);
}
