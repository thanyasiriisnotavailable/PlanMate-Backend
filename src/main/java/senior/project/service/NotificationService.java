package senior.project.service;

import senior.project.dto.NotificationRequestDTO;

public interface NotificationService {
    void saveFcmToken(String token);

    void sendNotification(NotificationRequestDTO request);
}
