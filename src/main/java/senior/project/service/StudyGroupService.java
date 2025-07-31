package senior.project.service;

import org.springframework.http.ResponseEntity;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.dto.StudyGroupResponseDTO;

public interface StudyGroupService {

    StudyGroupResponseDTO getGroups();
    ResponseEntity<?> createGroup(GroupRequestDTO groupInfo);
    ResponseEntity<?> joinGroup(JoinGroupRequestDTO dto);
}
