package senior.project.dao;

import senior.project.entity.FocusSession;
import senior.project.enums.FocusStatus;

public interface FocusSessionDao {
    FocusSession save(FocusSession focusSession);
    FocusSession findById(String id);
    long sumFocusSecondsByUser(String uid);
    FocusSession findByUserUidAndStatus(String userUid, FocusStatus focusStatus);
}
