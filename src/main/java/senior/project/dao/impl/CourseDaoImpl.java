package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.CourseDao;
import senior.project.entity.Course;
import senior.project.entity.CourseId;
import senior.project.entity.Term;
import senior.project.repository.CourseRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseDaoImpl implements CourseDao {

    private final CourseRepository courseRepository;

    @Override
    public Course save(Course course) {
        courseRepository.save(course);
        return course;
    }

    @Override
    public void saveAll(List<Course> courses) {
        courseRepository.saveAll(courses);
    }

    @Override
    public List<Course> findByTerm(Term term) {
        return courseRepository.findByTerm(term);
    }

    @Override
    public Course findById(CourseId courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    @Override
    public void delete(Course existingCourse) {
        courseRepository.delete(existingCourse);
    }
}
