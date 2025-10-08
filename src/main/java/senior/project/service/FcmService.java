package senior.project.service;

import senior.project.dto.NotificationDTO;

public interface FcmService {
    void saveFcmToken(String token);

    void sendNotification(NotificationDTO notificationDTO);
}
