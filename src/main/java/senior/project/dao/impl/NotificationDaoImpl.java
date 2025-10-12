package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.NotificationDao;
import senior.project.entity.Notification;
import senior.project.repository.NotificationRepository;

@Repository
@RequiredArgsConstructor
public class NotificationDaoImpl implements NotificationDao {
    private final NotificationRepository notificationRepository;

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }
}
