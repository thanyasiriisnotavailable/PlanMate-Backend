package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.FocusSessionDao;
import senior.project.entity.FocusSession;
import senior.project.repository.FocusSessionRepository;

@Repository
@RequiredArgsConstructor
public class FocusSessionDaoImpl implements FocusSessionDao {
    private final FocusSessionRepository focusSessionRepository;

    @Override
    public FocusSession save(FocusSession focusSession) {
        return focusSessionRepository.save(focusSession);
    }
}
