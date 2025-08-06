package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.EndFocusSessionDTO;
import senior.project.dto.StartFocusSessionDTO;
import senior.project.dto.ToDoListResponseDTO;
import senior.project.dto.plan.SessionDTO;
import senior.project.entity.plan.Session;
import senior.project.service.SessionService;
import senior.project.util.DTOMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/todo")
    public ResponseEntity<ToDoListResponseDTO> getToDoList() {
        Map<String, List<SessionDTO>> dtoMap = sessionService.getToDoListSessions();

        ToDoListResponseDTO dto = new ToDoListResponseDTO(
                dtoMap.get("today"),
                dtoMap.get("tomorrow"),
                dtoMap.get("upcoming")
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startFocusSession(@RequestBody StartFocusSessionDTO request) {

        try {
            var response = sessionService.startFocusSession(request.getSessionId());
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
            var response = sessionService.endFocusSession(request.getFocusSessionId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to end session: " + e.getMessage());
        }
    }
}