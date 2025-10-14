package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.PendingNotificationDao;
import senior.project.entity.PendingNotification;
import senior.project.repository.PendingNotificationRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PendingNotificationDaoImpl implements PendingNotificationDao {

    private final PendingNotificationRepository pendingNotificationRepository;

    @Override
    public List<PendingNotification> findByTargetUserUid(String targetUserUid) {
        return pendingNotificationRepository.findByTargetUserUid(targetUserUid);
    }

    @Override
    public void save(PendingNotification pending) {
        pendingNotificationRepository.save(pending);
    }

    @Override
    public void delete(PendingNotification pending) {
        pendingNotificationRepository.delete(pending);
    }
}
