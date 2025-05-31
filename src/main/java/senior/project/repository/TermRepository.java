package senior.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import senior.project.entity.Term;
import senior.project.entity.User;

public interface TermRepository extends JpaRepository<Term, Long> {
    Term findByUser(User user);
}
