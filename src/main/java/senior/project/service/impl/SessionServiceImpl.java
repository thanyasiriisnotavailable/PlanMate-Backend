package senior.project.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.FocusSessionDao;
import senior.project.dao.GroupMemberDao;
import senior.project.dao.SessionDao;
import senior.project.dao.UserDao;
import senior.project.entity.FocusSession;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;
import senior.project.firebase.FirebaseFocusService;
import senior.project.service.SessionService;
import senior.project.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SessionDao sessionDao;
    private final FocusSessionDao focusSessionDao;
    private final GroupMemberDao groupMemberDao;
    private final FirebaseFocusService firebaseFocusService;
    private final UserDao userDao;

    @Override
    public Map<String, List<Session>> getToDoListSessions() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        if (user == null) {
            throw new NullPointerException("User not found for UID: " + userUid);
        }

        Map<String, List<Session>> sessionMap = new HashMap<>();
        sessionMap.put("today", sessionDao.getTodaySessions(user));
        sessionMap.put("tomorrow", sessionDao.getTomorrowSessions(user));
        sessionMap.put("future", sessionDao.getFutureSessions(user));
        return sessionMap;
    }

    @Override
    public Map<String, Object> startFocusSession(String sessionId) {
        Session session = sessionDao.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found or unauthorized.");
        }

        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);
        long durationSeconds = session.getDuration();

        LocalDateTime focusStart = LocalDateTime.now();

        FocusSession focusSession = FocusSession.builder()
                .user(user)
                .session(session)
                .focusStart(focusStart)
                .status(FocusStatus.FOCUSING)
                .build();

        focusSessionDao.save(focusSession);

        // Fetch display name
        String displayName = user.getEmail(); // fallback
        try {
            String firebaseName = FirebaseAuth.getInstance().getUser(userUid).getDisplayName();
            if (firebaseName != null && !firebaseName.isBlank()) {
                displayName = firebaseName;
            }
        } catch (FirebaseAuthException ignored) {
        }

        // Collect group IDs
        List<Long> groupIds = groupMemberDao.findByUser(user).stream()
                .map(member -> member.getGroup().getId())
                .toList();

        // Send to Firebase
        firebaseFocusService.writeFocusSession(
                focusSession.getId(),
                userUid,
                session.getSessionId(),
                durationSeconds,
                displayName,
                groupIds,
                focusStart,
                focusSession.getStatus()
        );

        return Map.of(
                "message", "Focus session started",
                "focusSessionId", focusSession.getId(),
                "sessionId", session.getSessionId(),
                "startTime", focusStart,
                "duration", durationSeconds
        );
    }
}