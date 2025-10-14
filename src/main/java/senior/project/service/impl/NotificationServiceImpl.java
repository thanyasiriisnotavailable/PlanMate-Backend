package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import senior.project.dao.NotificationDao;
import senior.project.dao.PendingNotificationDao;
import senior.project.dto.NotificationDTO;
import senior.project.dto.NotificationRequestDTO;
import senior.project.entity.Notification;
import senior.project.entity.PendingNotification;
import senior.project.entity.User;
import senior.project.enums.NotificationType;
import senior.project.service.NotificationService;
import senior.project.service.UserService;
import senior.project.util.SecurityUtil;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserService userService;
    private final NotificationDao notificationDao;
    private final PendingNotificationDao pendingNotificationDao;

    @Override
    public List<NotificationDTO> getNotificationsForCurrentUser() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        if (userUid == null) {
            log.warn("Authenticated UID is null — cannot fetch notifications.");
            throw new IllegalStateException("No authenticated user");
        }

        User user = userService.findByUid(userUid);
        if (user == null) {
            log.warn("User not found for uid={}", userUid);
            return List.of();
        }

        // Assuming notificationDao can fetch notifications by user
        List<Notification> notifications = notificationDao.getNotificationsByUser(user);

        // Map entities to DTOs
        return notifications.stream()
                .map(n -> NotificationDTO.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .type(n.getType())
                        .isRead(n.getIsRead())
                        .time(n.getTime())
                        .build())
                .toList();
    }

    @Override
    public void saveFcmToken(String token) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        if (userUid == null) {
            log.warn("Authenticated UID is null — cannot save FCM token.");
            throw new IllegalStateException("No authenticated user");
        }
        if (token == null || token.trim().isEmpty()) {
            log.warn("Empty FCM token received for uid={}", userUid);
            return;
        }

        try {
            userService.updateFcmToken(userUid, token);
            log.debug("Saved FCM token for uid={}", userUid);

            // Process pending notifications
            processPendingNotificationsForUser(userUid);
        } catch (Exception e) {
            log.error("Error saving FCM token for uid={}: {}", userUid, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendNotification(NotificationRequestDTO request) {
        String userUid = request.getUserUid();
        String token = userService.getFcmToken(userUid);
        User user = userService.findByUid(userUid);

        if (token == null || token.isEmpty()) {
            log.warn("No FCM token found for uid={} — notification not sent", userUid);
            savePendingNotification(request); // Save for later
            return;
        }

        String title = request.getTitle();
        String content = request.getContent();
        NotificationType type = request.getType();

        try {
            // Build proper FCM notification payload
            Message message = Message.builder()
                    .setToken(token)
                    .putData("title", title)
                    .putData("body", content)
                    .putData("type", type != null ? type.name() : "GENERAL")
                    .build();

            // Send the notification via Firebase
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM notification: {}", response);

            // Save notification to the database
            saveNotificationToDatabase(title, content, type, user);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending FCM notification for uid={}: {}", userUid, e.getMessage(), e);
        }
    }

    @Override
    public void markAsRead(Long id) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        if (userUid == null) {
            log.warn("Authenticated UID is null — cannot mark as read.");
            throw new IllegalStateException("No authenticated user");
        }

        User user = userService.findByUid(userUid);
        if (user == null) {
            throw new IllegalStateException("User not found for UID=" + userUid);
        }

        Notification notification = notificationDao.getNotificationById(id);
        if (notification == null) {
            throw new IllegalArgumentException("Notification not found: " + id);
        }

        // Ensure the user owns this notification
        if (!notification.getUser().getUid().equals(user.getUid())) {
            throw new SecurityException("You cannot modify someone else's notification");
        }

        notification.setIsRead(true);
        notificationDao.saveNotification(notification);
    }

    private void saveNotificationToDatabase(String title, String content, NotificationType type, User user) {
        try {
            senior.project.entity.Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .content(content)
                    .type(type)
                    .isRead(false)
                    .build();

            notificationDao.saveNotification(notification);
        } catch (Exception e) {
            log.error("Error saving notification entity: {}", e.getMessage(), e);
        }
    }

    private void savePendingNotification(NotificationRequestDTO request) {
        PendingNotification pending = PendingNotification.builder()
                .targetUserUid(request.getUserUid())
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                // The 'createdAt' field is automatically set by @CreationTimestamp
                .build();
        pendingNotificationDao.save(pending);
        log.info("Saved pending notification for user {}", request.getUserUid());
    }

    private void processPendingNotificationsForUser(String userUid) {
        List<PendingNotification> pendingList = pendingNotificationDao.findByTargetUserUid(userUid);
        if (pendingList.isEmpty()) {
            return; // Nothing to do
        }

        log.info("Found {} pending notifications for user {}. Processing now...", pendingList.size(), userUid);

        for (PendingNotification pending : pendingList) {
            // Create a request DTO from the pending entity
            NotificationRequestDTO request = NotificationRequestDTO.builder()
                    .userUid(pending.getTargetUserUid())
                    .title(pending.getTitle())
                    .content(pending.getContent())
                    .type(pending.getType())
                    .build();

            // Resend using the main method (this will now find a valid token)
            // Important: This call is NOT recursive because the token now exists
            sendNotification(request);

            // After successfully sending (or attempting), delete it from the queue
            pendingNotificationDao.delete(pending);
        }
        log.info("Finished processing pending notifications for user {}", userUid);
    }
}
