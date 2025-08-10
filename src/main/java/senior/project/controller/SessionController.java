package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.ToDoListResponseDTO;
import senior.project.dto.plan.SessionDTO;
import senior.project.service.SessionService;

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

    @GetMapping("/completed")
    public ResponseEntity<List<SessionDTO>> getCompletedSessions() {
        List<SessionDTO> completedSessions = sessionService.getCompletedSessions();
        return ResponseEntity.ok(completedSessions);
    }
}