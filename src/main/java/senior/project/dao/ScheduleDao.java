package senior.project.dao;

import senior.project.entity.User;
import senior.project.entity.plan.Schedule;

import java.util.List;

public interface ScheduleDao {
    List<Schedule> findByUser(User user);
    Schedule save(Schedule schedule);
}