package senior.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import senior.project.entity.StudyPreference;
import senior.project.entity.StudyPreferenceDto;
import senior.project.entity.User;
import senior.project.service.StudyPreferenceService;
import senior.project.service.UserService;
import senior.project.util.StudyPreferenceMapper;

@RestController
@RequestMapping("/study-preferences")
public class StudyPreferenceController {

    @Autowired
    private StudyPreferenceService preferenceService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyPreferenceMapper mapper;

    @PostMapping
    public ResponseEntity<StudyPreferenceDto> save(@RequestBody StudyPreferenceDto dto) {
        String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByUid(uid)
                .orElseThrow(() -> new IllegalStateException("User not found: " + uid));

        StudyPreference entity = mapper.toEntity(dto, user);
        entity.setUserUid(uid);

        StudyPreference saved = preferenceService.savePreference(entity);
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<StudyPreferenceDto> get() {
        String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return preferenceService.getPreference(uid)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<StudyPreferenceDto> update(@RequestBody StudyPreferenceDto dto) {
        String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByUid(uid)
                .orElseThrow(() -> new IllegalStateException("User not found: " + uid));

        return preferenceService.getPreference(uid).map(existingPreference -> {
            // Update fields
            existingPreference.setMinSessionDuration(dto.getMinSessionDuration());
            existingPreference.setMaxSessionDuration(dto.getMaxSessionDuration());
            existingPreference.setPreferredStudyTimes(dto.getPreferredStudyTimes());
            existingPreference.setRevisionFrequency(dto.getRevisionFrequency());
            existingPreference.setBreakDurations(dto.getBreakDurations());

            StudyPreference updated = preferenceService.savePreference(existingPreference);
            return ResponseEntity.ok(mapper.toDto(updated));
        }).orElseGet(() -> {
            // If preference doesn't exist, create new one
            StudyPreference newPreference = mapper.toEntity(dto, user);
            newPreference.setUserUid(uid);
            StudyPreference saved = preferenceService.savePreference(newPreference);
            return ResponseEntity.ok(mapper.toDto(saved));
        });
    }
}