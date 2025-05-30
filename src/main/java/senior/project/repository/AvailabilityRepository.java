package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Availability;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
