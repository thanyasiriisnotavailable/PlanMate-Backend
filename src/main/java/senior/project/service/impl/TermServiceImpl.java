package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.TermDao;
import senior.project.entity.Term;
import senior.project.service.TermService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermServiceImpl implements TermService {

    private final TermDao termDao;

    @Override
    public void save(Term term) {
        termDao.save(term);
    }
}
