package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.PendingNotification;

import java.util.List;

public interface PendingNotificationRepository extends JpaRepository<PendingNotification, Long> {
    List<PendingNotification> findByTargetUserUid(String targetUserUid);
}
