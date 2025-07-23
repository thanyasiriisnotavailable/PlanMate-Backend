package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.FocusSession;

public interface FocusSessionRepository extends JpaRepository<FocusSession, String> {
}