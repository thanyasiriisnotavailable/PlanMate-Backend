package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Assignment;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
