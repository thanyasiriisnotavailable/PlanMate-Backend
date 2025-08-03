package senior.project.dao;

import senior.project.entity.FocusSession;

public interface FocusSessionDao {
    FocusSession save(FocusSession focusSession);
    FocusSession findById(String id);
    long sumFocusSecondsByUser(String uid);
}
