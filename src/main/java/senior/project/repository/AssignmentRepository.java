package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Assignment;
import senior.project.entity.Course;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    List<Assignment> findByCourse(Course course);
}
