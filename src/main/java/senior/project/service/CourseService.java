package senior.project.service;

import senior.project.entity.Course;

import java.util.List;

public interface CourseService {
    void save(Course course);
    void saveAll(List<Course> courses);
}
