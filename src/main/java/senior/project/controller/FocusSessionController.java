package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.EndFocusSessionDTO;
import senior.project.dto.FocusSessionDTO;
import senior.project.dto.StartFocusSessionDTO;
import senior.project.entity.FocusSession;
import senior.project.service.FocusSessionService;
import senior.project.util.SecurityUtil;

@RestController
@RequestMapping("/focus")
@RequiredArgsConstructor
public class FocusSessionController {
    private final FocusSessionService focusSessionService;

    @GetMapping("/{id}")
    public ResponseEntity<FocusSessionDTO> getFocusSession(@PathVariable String id) {
        FocusSessionDTO focusSession = focusSessionService.getFocusSessionById(id);
        if (focusSession == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(focusSession);
    }

    @GetMapping("/active")
    public ResponseEntity<FocusSessionDTO> getActiveFocusSession() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        FocusSessionDTO focusSession = focusSessionService.getActiveFocusSessionForUser(userUid);
        if (focusSession == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(focusSession);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startFocusSession(@RequestBody StartFocusSessionDTO request) {

        try {
            var response = focusSessionService.startFocusSession(request.getSessionId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Unable to start session. Please try again later.");
        }
    }

    @PostMapping("/end")
    public ResponseEntity<?> endFocusSession(@RequestBody EndFocusSessionDTO request) {
        try {
            var response = focusSessionService.endFocusSession(request.getFocusSessionId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to end session: " + e.getMessage());
        }
    }
}
