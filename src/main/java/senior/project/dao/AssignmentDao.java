package senior.project.dao;

import senior.project.entity.Assignment;
import senior.project.entity.Course;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssignmentDao {
    void save(Assignment assignment);
    void saveAll(List<Assignment> assignments);
    List<Assignment> findByCourse(Course course);
    void deleteByCourse(Course existingCourse);
    boolean existsById(String id);
    Assignment findById(String id);
    void deleteAll(Collection<Assignment> values);
    void delete(Assignment existingAssignment);
}
