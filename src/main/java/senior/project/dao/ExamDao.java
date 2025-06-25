package senior.project.dao;

import senior.project.entity.Course;
import senior.project.entity.Exam;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamDao {
    void save(Exam exam);
    void saveAll(List<Exam> exams);
    List<Exam> findByCourse(Course course);
    void deleteByCourse(Course existingCourse);
    boolean existsById(String id);
    Exam findById(String id);
    void deleteAll(Collection<Exam> values);
    void delete(Exam existingExam);
}
