package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.CourseDao;
import senior.project.entity.Course;
import senior.project.repository.CourseRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseDaoImpl implements CourseDao {

    private final CourseRepository courseRepository;

    @Override
    public void save(Course course) {
        courseRepository.save(course);
    }

    @Override
    public void saveAll(List<Course> courses) {
        courseRepository.saveAll(courses);
    }
}
