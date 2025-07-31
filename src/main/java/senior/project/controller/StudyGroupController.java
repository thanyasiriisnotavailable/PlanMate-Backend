package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.dto.StudyGroupResponseDTO;
import senior.project.service.StudyGroupService;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService studyGroupService;

    @GetMapping
    public ResponseEntity<StudyGroupResponseDTO> getGroups() {
        StudyGroupResponseDTO dto = studyGroupService.getGroups();

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
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
