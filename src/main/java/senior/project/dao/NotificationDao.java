package senior.project.dao;

import senior.project.entity.Notification;
import senior.project.entity.User;

import java.util.List;

public interface NotificationDao {
    Notification getNotificationById(Long id);
    void saveNotification(Notification notification);
    List<Notification> getNotificationsByUser(User user);
}
