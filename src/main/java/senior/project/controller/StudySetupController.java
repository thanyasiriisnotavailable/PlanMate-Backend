package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.StudySetupDTO;
import senior.project.service.StudySetupService;

@RestController
@RequestMapping("/study-setup")
@RequiredArgsConstructor
public class StudySetupController {

    private final StudySetupService studySetupService;

    @GetMapping
    public ResponseEntity<StudySetupDTO> getStudySetup() {
        String uid = getAuthenticatedUid();
        StudySetupDTO dto = studySetupService.getStudySetup(uid);
        if (dto == null) {
            System.out.println("No study setup found");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Void> setupStudyPlan(@RequestBody StudySetupDTO dto) {
        String uid = getAuthenticatedUid();
        studySetupService.processStudySetup(uid, dto);
        return ResponseEntity.ok().build();
    }

    private String getAuthenticatedUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}