package senior.project.dao;

import senior.project.entity.Exam;

import java.util.List;
import java.util.Optional;

public interface ExamDao {
    void save(Exam exam);
    void saveAll(List<Exam> exams);
}
