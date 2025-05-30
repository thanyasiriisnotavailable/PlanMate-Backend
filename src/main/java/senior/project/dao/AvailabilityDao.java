package senior.project.dao;

import senior.project.entity.Availability;

import java.util.List;
import java.util.Optional;

public interface AvailabilityDao {
    void save(Availability availability);
    void saveAll(List<Availability> availabilities);
}
