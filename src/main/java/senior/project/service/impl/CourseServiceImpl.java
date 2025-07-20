package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.CourseDao;
import senior.project.entity.Course;
import senior.project.service.CourseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private  final CourseDao courseDao;

    @Override
    public void save(Course course) {
        courseDao.save(course);
    }

    @Override
    public void saveAll (List<Course> courses) {
        courseDao.saveAll(courses);
    }
}
