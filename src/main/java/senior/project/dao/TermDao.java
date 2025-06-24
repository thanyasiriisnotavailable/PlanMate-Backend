package senior.project.dao;

import senior.project.entity.Term;
import senior.project.entity.User;

import java.util.Optional;

public interface TermDao {
    Term save(Term term);
    Term findByUser(User user);
    Optional<Term> findById(Long id);
}