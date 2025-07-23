package senior.project.service;

import org.springframework.http.ResponseEntity;
import senior.project.dto.GroupRequestDTO;

public interface StudyGroupService {

    ResponseEntity<?> createGroup(GroupRequestDTO groupInfo);
}
