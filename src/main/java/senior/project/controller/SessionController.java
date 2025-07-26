package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.FocusSessionRequestDTO;
import senior.project.entity.plan.Session;
import senior.project.service.SessionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/todo")
    public ResponseEntity<?> getToDoList() {
        Map<String, List<Session>> toDoList = sessionService.getToDoListSessions();
        if (toDoList.isEmpty()) {
            return ResponseEntity.ok("You have no upcoming sessions.");
        }

        return ResponseEntity.ok(toDoList);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startFocusSession(@RequestBody FocusSessionRequestDTO request) {

        try {
            var response = sessionService.startFocusSession(request.getSessionId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Unable to start session. Please try again later.");
        }
    }
}