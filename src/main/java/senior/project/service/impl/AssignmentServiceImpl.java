package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.AssignmentDao;
import senior.project.entity.Assignment;
import senior.project.service.AssignmentService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentDao assignmentDao;

    @Override
    public void save(Assignment assignment) {
        assignmentDao.save(assignment);
    }

    @Override
    public void saveAll(List<Assignment> assignments) {
        assignmentDao.saveAll(assignments);
    }
}
