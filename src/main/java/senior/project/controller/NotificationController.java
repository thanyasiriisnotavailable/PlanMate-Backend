package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.NotificationDTO;
import senior.project.dto.NotificationRequestDTO;
import senior.project.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotification() {
        List<NotificationDTO> notifications = notificationService.getNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/token")
    public ResponseEntity<?> saveFcmToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        notificationService.saveFcmToken(token);
        return ResponseEntity.ok("Token saved");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNoti(@RequestBody NotificationRequestDTO request) {
        try {
            notificationService.sendNotification(request);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Notification failed — saved to pending");
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }
}