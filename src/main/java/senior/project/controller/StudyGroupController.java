package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.GroupMemberProgressDTO;
import senior.project.dto.GroupRequestDTO;
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

    @PostMapping("/join/{joinCode}")
    public ResponseEntity<?> joinGroup(@PathVariable String joinCode) {
        return studyGroupService.joinGroup(joinCode);
    }

    @GetMapping("/{groupId}/progress")
    public ResponseEntity<?> getGroupProgress(
            @PathVariable Long groupId) {
        try {
            List<GroupMemberProgressDTO> progressList = studyGroupService.getGroupProgress(groupId);
            if (progressList.isEmpty()) {
                return ResponseEntity.ok("No progress data available for this schedule.");
            }
            return ResponseEntity.ok(progressList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unable to load schedule progress. Please try again later.");
        }
    }
}
