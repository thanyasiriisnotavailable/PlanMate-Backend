package senior.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.entity.StudyPreference;
import senior.project.service.StudyPreferenceService;

@RestController
@RequestMapping("/study-preferences")
public class StudyPreferenceController {

    @Autowired
    private StudyPreferenceService preferenceService;

    @PostMapping
    public ResponseEntity<StudyPreference> save(@RequestBody StudyPreference pref) {
        return ResponseEntity.ok(preferenceService.savePreference(pref));
    }

    @GetMapping("/{uid}")
    public ResponseEntity<StudyPreference> get(@PathVariable String uid) {
        return preferenceService.getPreference(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
