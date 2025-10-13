package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import senior.project.dao.NotificationDao;
import senior.project.dto.NotificationDTO;
import senior.project.dto.NotificationRequestDTO;
import senior.project.entity.Notification;
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
}
