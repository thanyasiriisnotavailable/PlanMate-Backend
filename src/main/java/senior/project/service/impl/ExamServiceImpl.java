package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.ExamDao;
import senior.project.entity.Exam;
import senior.project.service.ExamService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamDao examDao;

    @Override
    public void save(Exam exam) {
        examDao.save(exam);
    }

    @Override
    public void saveAll(List<Exam> exams) {
        examDao.saveAll(exams);
    }
}
