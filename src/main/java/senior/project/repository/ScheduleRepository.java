package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUser(User user);
}