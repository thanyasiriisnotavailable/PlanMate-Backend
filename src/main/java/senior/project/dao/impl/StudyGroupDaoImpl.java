package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.StudyGroupDao;
import senior.project.entity.StudyGroup;
import senior.project.repository.StudyGroupRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudyGroupDaoImpl implements StudyGroupDao {

    private final StudyGroupRepository studyGroupRepository;

    @Override
    public StudyGroup save(StudyGroup studyGroup) {
        return studyGroupRepository.save(studyGroup);
    }

    @Override
    public boolean existsByJoinCode(String code) {
        return studyGroupRepository.existsByJoinCode(code);
    }

    @Override
    public Optional<StudyGroup> findByJoinCode(String joinCode) {
        return studyGroupRepository.findByJoinCode(joinCode);
    }
}