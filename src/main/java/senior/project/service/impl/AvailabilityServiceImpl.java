package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.AvailabilityDao;
import senior.project.entity.Availability;
import senior.project.service.AvailabilityService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityDao availabilityDao;

    @Override
    public void save(Availability availability) {
        availabilityDao.save(availability);
    }

    @Override
    public void saveAll(List<Availability> availabilities) {
        availabilityDao.saveAll(availabilities);
    }
}
