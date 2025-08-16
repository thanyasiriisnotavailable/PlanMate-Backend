package senior.project.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import senior.project.enums.FocusStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
            String userImage,
            List<Long> groupIds,
            LocalDateTime focusStart,
            FocusStatus status
    ) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();

        long startMillis = focusStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = startMillis + durationSeconds * 1000;

        // Prepare focus session data
        Map<String, Object> focusData = new HashMap<>();
        focusData.put("id", focusSessionId);
        focusData.put("userId", userId);
        focusData.put("sessionId", sessionId);
        focusData.put("status", status.name());
        focusData.put("startedAt", startMillis);
        focusData.put("endsAt", endMillis);
        focusData.put("duration", durationSeconds);

        // Prepare active user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("image", userImage);
        userData.put("focusMode", true);
        userData.put("focusSessionId", focusSessionId);

        // Convert groups to {groupId: true}
        Map<String, Object> groupsMap = new HashMap<>();
        for (Long groupId : groupIds) {
            groupsMap.put(String.valueOf(groupId), true);
        }
        userData.put("groups", groupsMap);

        // Multi-path update (atomic write)
        Map<String, Object> updates = new HashMap<>();
        updates.put("/focusSessions/" + focusSessionId, focusData);
        updates.put("/activeUsers/" + userId, userData);
        for (Long groupId : groupIds) {
            updates.put("/activeGroups/" + groupId + "/" + userId, true);
        }

        rootRef.updateChildrenAsync(updates);

        // Handle onDisconnect cleanup
        DatabaseReference userRef = firebaseDatabase.getReference("activeUsers").child(userId);
        userRef.onDisconnect().removeValue((error, ref) -> {
            if (error != null) {
                System.err.println("Failed to set onDisconnect for user: " + error.getMessage());
            }
        });

        for (Long groupId : groupIds) {
            DatabaseReference groupRef = firebaseDatabase.getReference("activeGroups")
                    .child(String.valueOf(groupId))
                    .child(userId);
            groupRef.onDisconnect().removeValue((error, ref) -> {
                if (error != null) {
                    System.err.println("Failed to set onDisconnect for group " + groupId + ": " + error.getMessage());
                }
            });
        }
    }

    public void clearFocusSession(String focusSessionId, String userId, List<Long> groupIds) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        Map<String, Object> removals = new HashMap<>();
        removals.put("/focusSessions/" + focusSessionId, null);
        removals.put("/activeUsers/" + userId, null);

        for (Long groupId : groupIds) {
            removals.put("/activeGroups/" + groupId + "/" + userId, null);
        }

        firebaseDatabase.getReference().updateChildrenAsync(removals);
    }
}