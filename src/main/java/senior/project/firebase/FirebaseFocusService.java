package senior.project.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import senior.project.enums.FocusStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FirebaseFocusService {

    public void writeFocusSession(
            String focusSessionId,
            String userId,
            String sessionId,
            long durationSeconds,
            String userName,
            List<Long> groupIds,
            LocalDateTime focusStart,
            FocusStatus status
    ) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        // Write to focusSessions
        DatabaseReference focusRef = firebaseDatabase.getReference("focusSessions").child(focusSessionId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusSeconds(durationSeconds);

        Map<String, Object> focusData = new HashMap<>();
        focusData.put("id", focusSessionId);
        focusData.put("userId", userId);
        focusData.put("sessionId", sessionId);
        focusData.put("status", status.name());
        focusData.put("startedAt", now.toString());
        focusData.put("endsAt", focusStart.plusSeconds(durationSeconds));
        focusData.put("duration", durationSeconds);
        focusData.put("remaining", durationSeconds);

        focusRef.setValueAsync(focusData ); // Write asynchronously

        // Write to activeUsers
        DatabaseReference userRef = firebaseDatabase.getReference("activeUsers").child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("focusMode", true);
        userData.put("groups", groupIds);  // list of group IDs
        userData.put("focusSessionId", focusSessionId); // helps frontend map

        userRef.setValueAsync(userData);

        for (Long groupId : groupIds) {
            DatabaseReference groupRef = firebaseDatabase.getReference("activeGroups").child("group" + groupId);
            groupRef.child(userId).setValueAsync(true); // simple marker
        }
    }
}