package senior.project.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseFocusService {

    public void writeFocusSession(String focusSessionId, String userId, String sessionId, long durationSeconds) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(); // get instance here, after FirebaseApp init

        DatabaseReference ref = firebaseDatabase.getReference("focusSessions").child(focusSessionId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusSeconds(durationSeconds);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("sessionId", sessionId);
        data.put("status", "FOCUSING");
        data.put("startedAt", now.toString());
        data.put("endsAt", endsAt.toString());
        data.put("duration", durationSeconds);
        data.put("remaining", durationSeconds);

        ref.setValueAsync(data); // Write asynchronously
    }
}