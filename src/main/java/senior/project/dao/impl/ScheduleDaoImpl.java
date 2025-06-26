package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.ScheduleDao;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import senior.project.repository.ScheduleRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleDaoImpl implements ScheduleDao {

    private final ScheduleRepository scheduleRepository;

    @Override
    public List<Schedule> findByUser(User user) {
        return scheduleRepository.findByUser(user);
    }

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}