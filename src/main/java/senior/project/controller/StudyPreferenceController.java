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
        StudyPreferenceDTO saved = preferenceService.saveOrUpdate(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<StudyPreferenceDTO> get() {
        StudyPreferenceDTO preferenceDTO = preferenceService.getPreference();
        return ResponseEntity.ok(preferenceDTO);
    }

    @PutMapping
    public ResponseEntity<StudyPreferenceDTO> update(@RequestBody StudyPreferenceDTO dto) {
        StudyPreferenceDTO updated = preferenceService.saveOrUpdate(dto);
        return ResponseEntity.ok(updated);
    }
}