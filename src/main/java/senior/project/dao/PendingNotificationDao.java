package senior.project.dao;

import senior.project.entity.PendingNotification;

import java.util.List;

public interface PendingNotificationDao {
    List<PendingNotification> findByTargetUserUid(String targetUserUid);
    void save(PendingNotification pending);
    void delete(PendingNotification pending);
}
