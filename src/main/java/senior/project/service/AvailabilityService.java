package senior.project.service;

import senior.project.entity.Availability;

import java.util.List;

public interface AvailabilityService {
    void save(Availability availability);
    void saveAll(List<Availability> availabilities);
}
