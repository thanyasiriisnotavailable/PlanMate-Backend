package senior.project.dao;

import senior.project.entity.User;
import senior.project.entity.plan.Session;

import java.util.List;

public interface SessionDao {
    List<Session> getTodaySessions(User user);
    List<Session> getTomorrowSessions(User user);
    List<Session> getFutureSessions(User user);
    Session save(Session session);
    Session findById(String id);
}
