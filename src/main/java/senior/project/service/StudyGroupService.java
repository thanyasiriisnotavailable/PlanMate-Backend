package senior.project.service;

import org.springframework.http.ResponseEntity;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;

public interface StudyGroupService {

    ResponseEntity<?> createGroup(GroupRequestDTO groupInfo);
    ResponseEntity<?> joinGroup(JoinGroupRequestDTO dto);
}
