package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.ScheduleDao;
import senior.project.entity.Term;
import senior.project.entity.plan.Schedule;
import senior.project.entity.User;
import senior.project.repository.ScheduleRepository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Schedule> findById(String id) {
        return Optional.ofNullable(scheduleRepository.findById(id).orElse(null));
    }
}