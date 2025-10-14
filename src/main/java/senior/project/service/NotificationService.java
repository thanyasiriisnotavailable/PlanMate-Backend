package senior.project.service;

import senior.project.dto.NotificationDTO;
import senior.project.dto.NotificationRequestDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotificationsForCurrentUser();
    void saveFcmToken(String token);
    void sendNotification(NotificationRequestDTO request);
    void markAsRead(Long id);
}
