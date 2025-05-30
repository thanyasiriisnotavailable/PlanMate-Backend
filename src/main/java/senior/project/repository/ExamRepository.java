package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {
}
