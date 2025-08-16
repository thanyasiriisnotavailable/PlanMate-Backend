package senior.project.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.FocusSessionDao;
import senior.project.dao.GroupMemberDao;
import senior.project.dao.SessionDao;
import senior.project.dao.UserDao;
import senior.project.dto.FocusSessionDTO;
import senior.project.entity.FocusSession;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;
import senior.project.firebase.FirebaseFocusService;
import senior.project.service.FocusSessionService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FocusSessionServiceImpl implements FocusSessionService {
    private final FocusSessionDao focusSessionDao;
    private final SessionDao sessionDao;
    private final GroupMemberDao groupMemberDao;
    private final UserDao userDao;
    private final FirebaseFocusService firebaseFocusService;
    private final FirebaseAuth firebaseAuth;
    private final DTOMapper dtoMapper;

    @Override
    public FocusSessionDTO getFocusSessionById(String id) {
        FocusSession  focusSession = focusSessionDao.findById(id);
        return dtoMapper.toFocusSessionDto(focusSession);
    }

    @Override
    public FocusSessionDTO getActiveFocusSessionForUser(String userUid) {
        FocusSession focusSession = focusSessionDao.findByUserUidAndStatus(userUid, FocusStatus.FOCUSING);
        return dtoMapper.toFocusSessionDto(focusSession);
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
                .course(session.getCourse())
                .topic(session.getTopic())
                .focusStart(focusStart)
                .status(FocusStatus.FOCUSING)
                .sessionType(session.getType())
                .plannedDuration(session.getDuration())
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
        try {
            UserRecord userRecord = firebaseAuth.getUser(userUid);
            String imageUrl = userRecord.getPhotoUrl();

            firebaseFocusService.writeFocusSession(
                    focusSession.getId(),
                    userUid,
                    session.getSessionId(),
                    durationSeconds,
                    displayName,
                    imageUrl,
                    groupIds,
                    focusStart,
                    focusSession.getStatus()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Firebase sync failed: " + ex.getMessage());
        }

        return Map.of(
                "message", "Focus session started",
                "focusSessionId", focusSession.getId(),
                "sessionId", session.getSessionId(),
                "startTime", focusStart,
                "duration", durationSeconds
        );
    }

    @Override
    @Transactional
    public FocusSessionDTO endFocusSession(String focusSessionId) {
        FocusSession focusSession = focusSessionDao.findById(focusSessionId);

        if (focusSession.getFocusEnd() != null || focusSession.getStatus() == FocusStatus.COMPLETED) {
            throw new IllegalStateException("This session has already been completed.");
        }

        LocalDateTime now = LocalDateTime.now();
        focusSession.setFocusEnd(now);
        focusSession.setElapsedSeconds(
                java.time.Duration.between(focusSession.getFocusStart(), now).getSeconds()
        );
        focusSession.setStatus(FocusStatus.COMPLETED);

        Session relatedSession = focusSession.getSession();
        if (relatedSession != null) {
            relatedSession.setIsCompleted(true);
            sessionDao.save(relatedSession);
        }

        focusSessionDao.save(focusSession);

        // Cleanup Firebase
        try {
            List<Long> groupIds = groupMemberDao.findByUser(focusSession.getUser())
                    .stream()
                    .map(member -> member.getGroup().getId())
                    .toList();

            firebaseFocusService.clearFocusSession(
                    focusSessionId,
                    focusSession.getUser().getUid(),
                    groupIds
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Firebase cleanup failed: " + ex.getMessage());
        }

        return dtoMapper.toFocusSessionDto(focusSession);
    }
}
