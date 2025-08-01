package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.dto.StudyGroupResponseDTO;
import senior.project.service.StudyGroupService;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService studyGroupService;

    @GetMapping
    public ResponseEntity<List<StudyGroupResponseDTO>> getGroups() {
        List<StudyGroupResponseDTO> groups = studyGroupService.getGroups();
        return ResponseEntity.ok(groups);
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupRequestDTO dto) {
        return studyGroupService.createGroup(dto);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@RequestBody JoinGroupRequestDTO dto) {
        return studyGroupService.joinGroup(dto);
    }
}
