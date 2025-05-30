package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Term;

public interface TermRepository extends JpaRepository<Term, Long> {
}
