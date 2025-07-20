package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Course;
import senior.project.entity.Exam;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, String> {
    List<Exam> findByCourse(Course course);
}
