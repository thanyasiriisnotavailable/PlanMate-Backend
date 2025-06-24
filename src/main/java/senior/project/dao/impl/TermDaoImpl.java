package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.TermDao;
import senior.project.entity.Term;
import senior.project.entity.User;
import senior.project.repository.TermRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TermDaoImpl implements TermDao {

    private final TermRepository termRepository;

    @Override
    public Term save(Term term) {
        return termRepository.save(term);
    }

    @Override
    public Term findByUser(User user) {
        return termRepository.findByUser(user);
    }

    @Override
    public Optional<Term> findById(Long id) {
        return termRepository.findById(id);
    }
}
