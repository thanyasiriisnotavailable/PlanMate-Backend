package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.plan.Schedule;
import senior.project.entity.plan.Session;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findBySchedule(Schedule schedule);
}
