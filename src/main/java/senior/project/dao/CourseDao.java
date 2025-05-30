package senior.project.dao;

import senior.project.entity.Course;

import java.util.List;
import java.util.Optional;

public interface CourseDao {
    void save(Course course);
    void saveAll(List<Course> courses);
}
