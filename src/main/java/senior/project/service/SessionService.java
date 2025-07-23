package senior.project.service;

import senior.project.entity.User;
import senior.project.entity.plan.Session;

import java.util.List;
import java.util.Map;

public interface SessionService {
    Map<String, List<Session>> getToDoListSessions();
    Map<String, Object> startFocusSession(String sessionId);

}
