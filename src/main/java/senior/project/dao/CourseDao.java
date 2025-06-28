package senior.project.dao;

import senior.project.entity.Course;
import senior.project.entity.Term;

import java.util.List;

public interface CourseDao {
    Course save(Course course);
    void saveAll(List<Course> courses);
    List<Course> findByTerm(Term term);
    Course findById(Long courseId);
    void delete(Course existingCourse);
    void flush();
}
