package senior.project.dao;

import senior.project.entity.Notification;

public interface NotificationDao {
    Notification getNotificationById(Long id);
    void saveNotification(Notification notification);
}
