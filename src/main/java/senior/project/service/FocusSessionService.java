package senior.project.service;

import jakarta.transaction.Transactional;
import senior.project.dto.FocusSessionDTO;
import senior.project.entity.FocusSession;

import java.util.Map;

public interface FocusSessionService {

    FocusSessionDTO getFocusSessionById(String id);
    FocusSessionDTO getActiveFocusSessionForUser(String userUid);
    Map<String, Object> startFocusSession(String sessionId);

    @Transactional
    FocusSessionDTO pauseFocusSession(String focusSessionId);

    @Transactional
    FocusSessionDTO resumeFocusSession(String focusSessionId);

    FocusSessionDTO endFocusSession(String focusSessionId);

    @Transactional
    Map<String, Object> joinSharedFocusRoom(String roomId);

    Object inviteUserToSharedRoom(String targetUserId);

    Object declineInvitation(String invitationId);
}
