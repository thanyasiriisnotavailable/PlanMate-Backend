package senior.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UtilController {

    // sync user authentication
    @GetMapping("/ping")
    public ResponseEntity<Void> handlePing() {
        // The FirebaseTokenVerifier will have already run and handled user sync.
        // This controller just needs to exist to provide a successful response.
        return ResponseEntity.ok().build();
    }
}
