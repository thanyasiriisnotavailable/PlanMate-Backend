package senior.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.User;
import senior.project.service.StudyPreferenceService;
import senior.project.service.UserService;
import senior.project.util.AppMapper;

@RestController
@RequestMapping("/study-preferences")
public class StudyPreferenceController {

    @Autowired
    private StudyPreferenceService preferenceService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppMapper mapper;

    @PostMapping
    public ResponseEntity<StudyPreferenceDTO> save(@RequestBody StudyPreferenceDTO dto) {
        String uid = getAuthenticatedUid();
        User user = userService.findByUid(uid)
                .orElseThrow(() -> new IllegalStateException("User not found: " + uid));

        StudyPreference entity = mapper.toStudyPreference(dto, user);
        StudyPreference saved = preferenceService.savePreference(entity);
        return ResponseEntity.ok(mapper.toStudyPreferenceDto(saved));
    }

    @GetMapping
    public ResponseEntity<StudyPreferenceDTO> get() {
        String uid = getAuthenticatedUid();
        return preferenceService.getPreference(uid)
                .map(mapper::toStudyPreferenceDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<StudyPreferenceDTO> update(@RequestBody StudyPreferenceDTO dto) {
        String uid = getAuthenticatedUid();
        User user = userService.findByUid(uid)
                .orElseThrow(() -> new IllegalStateException("User not found: " + uid));

        return preferenceService.getPreference(uid).map(existing -> {
            existing.setMinSessionDuration(dto.getMinSessionDuration());
            existing.setMaxSessionDuration(dto.getMaxSessionDuration());
            existing.setPreferredStudyTimes(dto.getPreferredStudyTimes());
            existing.setRevisionFrequency(dto.getRevisionFrequency());
            existing.setBreakDurations(dto.getBreakDurations());

            StudyPreference updated = preferenceService.savePreference(existing);
            return ResponseEntity.ok(mapper.toStudyPreferenceDto(updated));
        }).orElseGet(() -> {
            StudyPreference newPref = mapper.toStudyPreference(dto, user);
            StudyPreference saved = preferenceService.savePreference(newPref);
            return ResponseEntity.ok(mapper.toStudyPreferenceDto(saved));
        });
    }

    private String getAuthenticatedUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}