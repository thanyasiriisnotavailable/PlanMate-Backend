package senior.project.service;

import senior.project.dto.FocusSessionDTO;
import senior.project.entity.FocusSession;

import java.util.Map;

public interface FocusSessionService {

    FocusSessionDTO getFocusSessionById(String id);
    FocusSessionDTO getActiveFocusSessionForUser(String userUid);
    Map<String, Object> startFocusSession(String sessionId);
    FocusSessionDTO endFocusSession(String focusSessionId);
}
