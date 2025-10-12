package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.NotificationDao;
import senior.project.entity.Notification;
import senior.project.entity.User;
import senior.project.repository.NotificationRepository;

import java.util.List;

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

    @Override
    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByTimeDesc(user);
    }
}
