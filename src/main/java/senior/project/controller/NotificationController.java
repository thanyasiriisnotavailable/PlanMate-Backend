package senior.project.controller;

import com.google.api.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import senior.project.dto.NotificationDTO;
import senior.project.service.FcmService;
import senior.project.service.UserService;
import senior.project.util.SecurityUtil;

import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final FcmService fcmService;

    @PostMapping("/token")
    public ResponseEntity<?> saveFcmToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        fcmService.saveFcmToken(token);
        return ResponseEntity.ok("Token saved");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNoti(@RequestBody NotificationDTO request) {
        fcmService.sendNotification(request);
        return ResponseEntity.ok("Notification sent");
    }
}