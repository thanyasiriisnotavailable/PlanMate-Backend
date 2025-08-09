package senior.project.service;

import senior.project.dto.plan.SessionDTO;
import senior.project.entity.FocusSession;

import java.util.List;
import java.util.Map;

public interface SessionService {
    Map<String, List<SessionDTO>> getToDoListSessions();
}
