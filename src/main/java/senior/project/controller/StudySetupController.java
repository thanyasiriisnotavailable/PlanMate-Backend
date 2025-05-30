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