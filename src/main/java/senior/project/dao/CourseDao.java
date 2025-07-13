package senior.project.dao;

import senior.project.entity.Course;
import senior.project.entity.Term;

import java.util.List;
import java.util.Set;

public interface CourseDao {
    Course save(Course course);
    List<Course> saveAll(List<Course> courses);
    List<Course> findByTerm(Term term);
    Course findById(Long courseId);
    void delete(Course existingCourse);
    void flush();
    void deleteAllByIdInBatch(Set<Long> idsToDelete);
}
