package senior.project.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.FocusSessionDao;
import senior.project.dao.GroupMemberDao;
import senior.project.dao.SessionDao;
import senior.project.dao.UserDao;
import senior.project.dto.plan.SessionDTO;
import senior.project.entity.FocusSession;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;
import senior.project.firebase.FirebaseFocusService;
import senior.project.service.SessionService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SessionDao sessionDao;
    private final UserDao userDao;
    private final DTOMapper dtoMapper;

    @Override
    public Map<String, List<SessionDTO>> getToDoListSessions() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        if (user == null) {
            throw new NullPointerException("User not found for UID: " + userUid);
        }

        Map<String, List<Session>> sessionMap = new HashMap<>();
        sessionMap.put("today", sessionDao.getTodaySessions(user));
        sessionMap.put("tomorrow", sessionDao.getTomorrowSessions(user));
        sessionMap.put("upcoming", sessionDao.getFutureSessions(user));

        // Convert to DTOs
        Map<String, List<SessionDTO>> dtoMap = new HashMap<>();
        sessionMap.forEach((key, sessions) ->
                dtoMap.put(key, sessions.stream().map(dtoMapper::toSessionDto).toList())
        );

        return dtoMap;
    }
}