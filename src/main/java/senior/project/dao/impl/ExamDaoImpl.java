package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.ExamDao;
import senior.project.entity.Course;
import senior.project.entity.Exam;
import senior.project.entity.Topic;
import senior.project.repository.ExamRepository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ExamDaoImpl implements ExamDao {

    private final ExamRepository examRepository;

    @Override
    public void save(Exam exam) {
        examRepository.save(exam);
    }

    @Override
    public void saveAll(List<Exam> exams) {
        examRepository.saveAll(exams);
    }
    @Override
    public List<Exam> findByCourse(Course course) {
        return examRepository.findByCourse(course);
    }

}
