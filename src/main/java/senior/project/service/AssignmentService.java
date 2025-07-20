package senior.project.service;

import senior.project.entity.Assignment;

import java.util.List;

public interface AssignmentService {
    void save(Assignment assignment);
    void saveAll(List<Assignment> assignments);
}
