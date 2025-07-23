package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.SessionDao;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.repository.SessionRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SessionDaoImpl implements SessionDao {
    private final SessionRepository sessionRepository;

    @Override
    public List<Session> getTodaySessions(User user) {
        return sessionRepository.findBySchedule_UserAndDate(user, LocalDate.now());
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
}