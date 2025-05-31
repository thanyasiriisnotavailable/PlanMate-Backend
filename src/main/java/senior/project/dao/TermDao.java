package senior.project.dao;

import senior.project.entity.Term;
import senior.project.entity.User;

import java.util.List;
import java.util.Optional;

public interface TermDao {
    void save(Term term);
    Term findByUser(User user);
}