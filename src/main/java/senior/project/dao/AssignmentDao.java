package senior.project.dao;

import senior.project.entity.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentDao {
    void save(Assignment assignment);
    void saveAll(List<Assignment> assignments);
}
