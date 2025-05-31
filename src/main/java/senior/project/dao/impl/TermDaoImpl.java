package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.TermDao;
import senior.project.entity.Term;
import senior.project.entity.User;
import senior.project.repository.TermRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TermDaoImpl implements TermDao {

    private final TermRepository termRepository;

    @Override
    public void save(Term term) {
        termRepository.save(term);
    }

    @Override
    public Term findByUser(User user) {
        return termRepository.findByUser(user);
    }
}
