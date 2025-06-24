package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Course;
import senior.project.entity.CourseId;
import senior.project.entity.Term;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, CourseId> {
    List<Course> findByTerm(Term term);
}
