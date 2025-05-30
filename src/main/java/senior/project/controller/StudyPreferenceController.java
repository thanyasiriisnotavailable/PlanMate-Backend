package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.User;
import senior.project.service.StudyPreferenceService;
import senior.project.service.UserService;
import senior.project.util.DTOMapper;

@RestController
@RequestMapping("/study-preferences")
@RequiredArgsConstructor
public class StudyPreferenceController {

    private final StudyPreferenceService preferenceService;

    @PostMapping
    public ResponseEntity<StudyPreferenceDTO> save(@RequestBody StudyPreferenceDTO dto) {
        String uid = getAuthenticatedUid();
        StudyPreferenceDTO saved = preferenceService.saveOrUpdate(uid, dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<StudyPreferenceDTO> get() {
        String uid = getAuthenticatedUid();
        return preferenceService.getPreference(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<StudyPreferenceDTO> update(@RequestBody StudyPreferenceDTO dto) {
        String uid = getAuthenticatedUid();
        StudyPreferenceDTO updated = preferenceService.saveOrUpdate(uid, dto);
        return ResponseEntity.ok(updated);
    }

    private String getAuthenticatedUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}