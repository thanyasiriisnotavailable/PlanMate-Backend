package senior.project.service;

import senior.project.entity.Exam;

import java.util.List;

public interface ExamService {
    void save(Exam exam);
    void saveAll(List<Exam> exams);
}
