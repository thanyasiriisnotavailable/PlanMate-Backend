package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import senior.project.dto.NotificationRequestDTO;
import senior.project.service.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    public ResponseEntity<?> saveFcmToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        notificationService.saveFcmToken(token);
        return ResponseEntity.ok("Token saved");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNoti(@RequestBody NotificationRequestDTO request) {
        notificationService.sendNotification(request);
        return ResponseEntity.ok("Notification sent");
    }
}