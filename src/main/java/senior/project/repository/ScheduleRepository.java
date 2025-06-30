package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Term;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    List<Schedule> findByUser(User user);
}