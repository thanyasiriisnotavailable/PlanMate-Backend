package senior.project.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        FirebaseToken user = (FirebaseToken) request.getAttribute("firebaseUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("email", user.getEmail());
        profile.put("uid", user.getUid());
        profile.put("name", user.getName());
        profile.put("image", user.getPicture());

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/ping")
    public ResponseEntity<Void> handlePing() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> handleLogout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.replace("Bearer ", "");
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                String uid = decodedToken.getUid();
                FirebaseAuth.getInstance().revokeRefreshTokens(uid);
                return ResponseEntity.ok().build();
            } catch (FirebaseAuthException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}