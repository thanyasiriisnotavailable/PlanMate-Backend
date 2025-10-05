package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.SessionDao;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;
import senior.project.repository.SessionRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SessionDaoImpl implements SessionDao {
    private final SessionRepository sessionRepository;

    @Override
    public List<Session> getOverdueSessions(User user) {
        return sessionRepository.findOverdueSessions(user);
    }

    @Override
    public List<Session> getTodaySessions(User user) {
        return sessionRepository.findTodayActiveSessions(user);
    }

    @Override
    public List<Session> getTomorrowSessions(User user) {
        return sessionRepository.findBySchedule_UserAndDate(user, LocalDate.now().plusDays(1));
    }

    @Override
    public List<Session> getFutureSessions(User user) {
        return sessionRepository.findBySchedule_UserAndDateAfter(user, LocalDate.now().plusDays(1));
    }

    @Override
    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Session findById(String id) {
        return sessionRepository.findById(id).orElse(null);
    }

    @Override
    public int countTotalPlannedSessionsForUser(String uid) {
        return sessionRepository.countBySchedule_User_Uid(uid);
    }

    @Override
    public int countCompletedSessionsForUser(String uid) {
        return sessionRepository.countBySchedule_User_UidAndFocusSessions_Status(uid, FocusStatus.COMPLETED);
    }

    @Override
    public List<Session> getCompletedSessions(User user) {
        return sessionRepository.getCompletedSessions(user);
    }
}