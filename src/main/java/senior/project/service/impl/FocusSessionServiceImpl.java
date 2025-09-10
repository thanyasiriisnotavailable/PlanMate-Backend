package senior.project.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.google.api.pathtemplate.ValidationException;
import senior.project.firebase.FirebaseFocusService;
import senior.project.service.FocusSessionService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;
import java.util.Objects;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FocusSessionServiceImpl implements FocusSessionService {
    private static final Logger log = LoggerFactory.getLogger(FocusSessionServiceImpl.class);
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
        FocusSession focusingSession = focusSessionDao.findByUserUidAndStatus(userUid, FocusStatus.FOCUSING);
        if (focusingSession != null) {
            return dtoMapper.toFocusSessionDto(focusingSession);
        }

        FocusSession pausedSession = focusSessionDao.findByUserUidAndStatus(userUid, FocusStatus.PAUSED);
        if (pausedSession != null) {
            return dtoMapper.toFocusSessionDto(pausedSession);
        }

        return null;
    }

    @Override
    public Map<String, Object> startFocusSession(String sessionId) {
        Session session = sessionDao.findById(sessionId);
        if (session == null) {
            throw new ValidationException("Session not found or unauthorized.");
        }

        long durationSeconds = session.getDuration();
        if (durationSeconds <= 0) {
            throw new ValidationException("Focus duration must be greater than zero.");
        }

        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userDao.findByUid(userUid);

        FocusSessionDTO activeFocus = getActiveFocusSessionForUser(userUid);
        if (activeFocus != null) {
            throw new ValidationException("You already have an active focus session.");
        }

        LocalDateTime focusStart = LocalDateTime.now();

        FocusSession focusSession = FocusSession.builder()
                .user(user)
                .session(session)
                .course(session.getCourse())
                .topic(session.getTopic())
                .focusStart(focusStart)
                .elapsedSeconds(0L)
                .status(FocusStatus.FOCUSING)
                .sessionType(session.getType())
                .plannedDuration(session.getDuration())
                .build();

        focusSessionDao.save(focusSession);

        UserRecord rec = null;
        try {
            rec = firebaseAuth.getUser(userUid);
        } catch (FirebaseAuthException ignore) {
        }
        String displayName = (rec != null && rec.getDisplayName() != null && !rec.getDisplayName().isBlank())
                ? rec.getDisplayName() : user.getEmail();
        String imageUrl = (rec != null) ? rec.getPhotoUrl() : null;

        List<Long> groupIds = groupMemberDao.findByUser(user).stream()
                .map(member -> {
                    if (member.getGroup() == null) {
                        log.warn("User {} has a GroupMember with NULL group", user.getUid());
                        return null;
                    }
                    return member.getGroup().getId();
                })
                .filter(Objects::nonNull)
                .toList();

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

        return Map.of(
                "message", "Focus session started",
                "focusSessionId", focusSession.getId(),
                "sessionId", session.getSessionId(),
                "startTime", focusStart,
                "duration", durationSeconds,
                "status", focusSession.getStatus().toString(),
                "groupIds", groupIds
        );
    }

    @Transactional
    @Override
    public FocusSessionDTO pauseFocusSession(String focusSessionId) {
        FocusSession focusSession = focusSessionDao.findById(focusSessionId);

        // Check if the session is currently focusing
        if (focusSession == null || focusSession.getStatus() != FocusStatus.FOCUSING) {
            throw new ValidationException("Session is not active or not found.");
        }

        // Calculate elapsed time from the last start/resume
        LocalDateTime start = focusSession.getFocusStart();
        if (start == null) {
            throw new ValidationException("Focus session start time is not set.");
        }
        long secondsFromLastAction = java.time.Duration.between(start, LocalDateTime.now()).getSeconds();

        // Update elapsedSeconds and status
        focusSession.setElapsedSeconds(focusSession.getElapsedSeconds() + secondsFromLastAction);
        focusSession.setStatus(FocusStatus.PAUSED);

        focusSessionDao.save(focusSession);

        // Update Firebase to reflect the paused state
        try {
            firebaseFocusService.updateFocusSession(
                    focusSessionId,
                    focusSession.getUser().getUid(),
                    focusSession.getStatus(),
                    focusSession.getElapsedSeconds()
            );
        } catch (Exception ex) {
            log.warn("Firebase sync failed for focusSessionId={} userUid={}: {}", focusSession.getId(), focusSession.getUser().getUid(), ex.getMessage());
            log.debug("Firebase sync exception", ex);
        }

        return dtoMapper.toFocusSessionDto(focusSession);
    }

    @Transactional
    @Override
    public FocusSessionDTO resumeFocusSession(String focusSessionId) {
        FocusSession focusSession = focusSessionDao.findById(focusSessionId);

        // Check if the session is currently paused
        if (focusSession == null || focusSession.getStatus() != FocusStatus.PAUSED) {
            throw new ValidationException("Session is not in a pausable state.");
        }

        // Reset focus start time to the current time
        focusSession.setFocusStart(LocalDateTime.now());
        focusSession.setStatus(FocusStatus.FOCUSING);

        focusSessionDao.save(focusSession);

        // Update Firebase to reflect the resumed state
        try {
            firebaseFocusService.resumeFocusSession(
                    focusSessionId,
                    focusSession.getUser().getUid(),
                    focusSession.getStatus(),
                    focusSession.getFocusStart(),
                    focusSession.getPlannedDuration()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return dtoMapper.toFocusSessionDto(focusSession);
    }

    @Override
    @Transactional
    public FocusSessionDTO endFocusSession(String focusSessionId) {
        FocusSession focusSession = focusSessionDao.findById(focusSessionId);

        if (focusSession == null) {
            throw new NullPointerException("Focus session not found.");
        }

        if (focusSession.getFocusEnd() != null || focusSession.getStatus() == FocusStatus.COMPLETED) {
            throw new ValidationException("This session has already been completed.");
        }
        // Check if the session is active (FOCUSING) before ending
        if (focusSession.getStatus() != FocusStatus.FOCUSING) {
            throw new ValidationException("This session is not currently active.");
        }

        // Calculate final elapsed seconds based on the last active period
        long secondsFromLastAction = java.time.Duration.between(focusSession.getFocusStart(), LocalDateTime.now()).getSeconds();
        long totalElapsed = focusSession.getElapsedSeconds() + secondsFromLastAction;

        // prevent duration < 5 min
        if (totalElapsed < 300) {
            throw new ValidationException("Focus session is too short. Minimum duration is 5 minutes.");
        }

        focusSession.setElapsedSeconds(totalElapsed);

        // Set end time and status
        focusSession.setFocusEnd(LocalDateTime.now());
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

    @Override
    @Transactional
    public Map<String, Object> inviteUserToSharedRoom(String targetUserId) {
        String userUid = SecurityUtil.getAuthenticatedUid();

        // Get A’s active session
        FocusSession focusSession = focusSessionDao.findByUserUidAndStatus(userUid, FocusStatus.FOCUSING);
        if (focusSession == null) {
            throw new ValidationException("You must have an active focus session to invite someone.");
        }

        // Generate roomId (based on inviter’s UID)
        String roomId = "room-" + userUid;

        // Get inviter info
        String displayName = focusSession.getUser().getEmail();
        String imageUrl = null;
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userUid);
            if (userRecord.getDisplayName() != null && !userRecord.getDisplayName().isBlank()) {
                displayName = userRecord.getDisplayName();
            }
            imageUrl = userRecord.getPhotoUrl();
        } catch (FirebaseAuthException error) {
            error.printStackTrace();
        }

        // Ensure inviter (User A) is already in the shared room
        firebaseFocusService.joinSharedFocusRoom(
                focusSession.getId(),
                userUid,
                roomId,
                focusSession.getPlannedDuration() - focusSession.getElapsedSeconds(),
                displayName,
                imageUrl
        );

        // Push invitation to Firebase
        String invitationId = UUID.randomUUID().toString();
        firebaseFocusService.sendInvitation(targetUserId, invitationId, userUid, displayName, roomId);

        return Map.of(
                "message", "Invitation sent successfully",
                "invitationId", invitationId,
                "roomId", roomId
        );
    }

    @Override
    @Transactional
    public Map<String, Object> joinSharedFocusRoom(String roomId) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        FocusSession focusSession = focusSessionDao.findByUserUidAndStatus(userUid, FocusStatus.FOCUSING);

        // Validate session and user
        if (focusSession == null) {
            throw new ValidationException("You must have an active focus session to join a shared room.");
        }

        // Check if the user is already in a shared room
        String existingRoomId = firebaseFocusService.getSharedRoomIdForUser(userUid);
        if (existingRoomId != null) {
            throw new ValidationException("You are already in a shared room with ID: " + existingRoomId);
        }

        // Check the number of active users in the room
        long activeMembersCount = firebaseFocusService.countActiveUsersInRoom(roomId);
        if (activeMembersCount >= 5) {
            throw new ValidationException("The shared focus room has reached the maximum of 5 members.");
        }

        // Get display name and image for Firebase
        String displayName = focusSession.getUser().getEmail();
        String imageUrl = null;
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userUid);
            if (userRecord.getDisplayName() != null && !userRecord.getDisplayName().isBlank()) {
                displayName = userRecord.getDisplayName();
            }
            imageUrl = userRecord.getPhotoUrl();
        } catch (FirebaseAuthException error) {
            error.printStackTrace();
        }

        // Push to Firebase for the shared room
        try {
            firebaseFocusService.joinSharedFocusRoom(
                    focusSession.getId(),
                    userUid,
                    roomId,
                    focusSession.getPlannedDuration() - focusSession.getElapsedSeconds(), // Remaining time
                    displayName,
                    imageUrl
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Firebase sync failed for joining shared room: " + ex.getMessage());
            throw new RuntimeException("Failed to join shared session on Firebase.");
        }

        return Map.of("message", "Successfully joined shared focus room", "roomId", roomId);
    }

    @Override
    @Transactional
    public Map<String, Object> declineInvitation(String invitationId) {
        String userUid = SecurityUtil.getAuthenticatedUid();

        boolean success = firebaseFocusService.removeInvitation(userUid, invitationId);

        if (!success) {
            throw new ValidationException("Invitation not found or already handled.");
        }

        return Map.of(
                "message", "Invitation declined successfully",
                "invitationId", invitationId
        );
    }
}
