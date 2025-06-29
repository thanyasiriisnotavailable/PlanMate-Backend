package senior.project.dao;

import senior.project.entity.Availability;
import senior.project.entity.User;

import java.util.List;

public interface AvailabilityDao {
    Availability save(Availability availability);
    void saveAll(List<Availability> availabilities);
    List<Availability> findByUser(User user);
    void deleteByUser(User user);
}
