package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import senior.project.dto.NotificationDTO;
import senior.project.service.FcmService;
import com.google.firebase.messaging.*;
import senior.project.service.UserService;
import senior.project.util.SecurityUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {
    private final UserService userService;

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
    public void sendNotification(NotificationDTO notificationDTO) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        String token = userService.getFcmToken(userUid);
        String title = notificationDTO.getTitle();
        String body = notificationDTO.getBody();

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent notification: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            System.out.println("Error seding FCM notification: " + e.getMessage());
        }
    }
}
