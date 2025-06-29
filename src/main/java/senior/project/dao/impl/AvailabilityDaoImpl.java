package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.AvailabilityDao;
import senior.project.entity.Availability;
import senior.project.entity.User;
import senior.project.repository.AvailabilityRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AvailabilityDaoImpl implements AvailabilityDao {

    private final AvailabilityRepository availabilityRepository;

    @Override
    public Availability save(Availability availability) {
        availabilityRepository.save(availability);
        return availability;
    }

    @Override
    public void saveAll(List<Availability> availabilities) {
        availabilityRepository.saveAll(availabilities);
    }

    @Override
    public List<Availability> findByUser(User user) {
        return availabilityRepository.findByUser(user);
    }

    @Override
    public void deleteByUser(User user) {
        availabilityRepository.deleteByUser(user);
    }
}
