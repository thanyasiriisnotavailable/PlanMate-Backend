package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.AssignmentDao;
import senior.project.entity.Assignment;
import senior.project.entity.Course;
import senior.project.entity.Topic;
import senior.project.repository.AssignmentRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssignmentDaoImpl implements AssignmentDao {

    private final AssignmentRepository assignmentRepository;

    @Override
    public void save(Assignment assignment) {
        assignmentRepository.save(assignment);
    }

    @Override
    public void saveAll(List<Assignment> assignments) {
        assignmentRepository.saveAll(assignments);
    }

    @Override
    public List<Assignment> findByCourse(Course course) {
        return assignmentRepository.findByCourse(course);
    }
}
