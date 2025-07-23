package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.service.StudyGroupService;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService studyGroupService;

    @PostMapping()
    public ResponseEntity<?> createGroup(@RequestBody GroupRequestDTO dto) {
        return studyGroupService.createGroup(dto);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@RequestBody JoinGroupRequestDTO dto) {
        return studyGroupService.joinGroup(dto);
    }
}
