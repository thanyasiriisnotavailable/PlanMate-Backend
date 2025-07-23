package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.FocusSessionDao;
import senior.project.dao.SessionDao;
import senior.project.dao.UserDao;
import senior.project.entity.FocusSession;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
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
    private final FirebaseFocusService firebaseFocusService;
    private final UserDao userDao;

    @Override
    public Map<String, List<Session>> getToDoListSessions() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

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

        FocusSession focusSession = FocusSession.builder()
                .user(user)
                .session(session)
                .focusStart(LocalDateTime.now())
                .completed(false)
                .build();

        focusSessionDao.save(focusSession);

        firebaseFocusService.writeFocusSession(
                focusSession.getId(),
                user.getUid(),
                session.getSessionId(),
                durationSeconds
        );

        return Map.of(
                "message", "Focus session started",
                "focusSessionId", focusSession.getId(),
                "sessionId", session.getSessionId(),
                "startTime", focusSession.getFocusStart(),
                "duration", durationSeconds
        );
    }
}