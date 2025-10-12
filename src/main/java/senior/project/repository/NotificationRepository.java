package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import senior.project.entity.Notification;
import senior.project.entity.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.time DESC")
    List<Notification> findByUserOrderByTimeDesc(User user);
}
