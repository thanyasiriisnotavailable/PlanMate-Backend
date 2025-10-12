package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
