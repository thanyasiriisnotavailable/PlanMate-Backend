package senior.project.firebase;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import senior.project.enums.FocusStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            groupsMap.put("group_" + groupId, true);
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

    public void updateFocusSession(
            String focusSessionId,
            String userId,
            FocusStatus status,
            long elapsedSeconds
    ) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference sessionRef = firebaseDatabase.getReference("focusSessions").child(focusSessionId);
        DatabaseReference userRef = firebaseDatabase.getReference("activeUsers").child(userId);

        // Prepare updates for the focus session data
        Map<String, Object> sessionUpdates = new HashMap<>();
        sessionUpdates.put("status", status.name());
        sessionUpdates.put("elapsedSeconds", elapsedSeconds);

        // If a session is paused, the end time is no longer relevant
        if (status == FocusStatus.PAUSED) {
            sessionUpdates.put("endsAt", null);
        }

        // Update the data in Firebase
        sessionRef.updateChildrenAsync(sessionUpdates);

        // Also update the user's status in the activeUsers node if necessary
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("focusMode", status == FocusStatus.FOCUSING);
        userRef.updateChildrenAsync(userUpdates);
    }

    public void resumeFocusSession(
            String focusSessionId,
            String userId,
            FocusStatus status,
            LocalDateTime focusStart,
            long plannedDuration
    ) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference sessionRef = firebaseDatabase.getReference("focusSessions").child(focusSessionId);
        DatabaseReference userRef = firebaseDatabase.getReference("activeUsers").child(userId);

        long startMillis = focusStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = startMillis + plannedDuration * 1000;

        // Prepare updates for the focus session data
        Map<String, Object> sessionUpdates = new HashMap<>();
        sessionUpdates.put("status", status.name());
        sessionUpdates.put("startedAt", startMillis);
        sessionUpdates.put("endsAt", endMillis); // Update the new planned end time

        // Update the data in Firebase
        sessionRef.updateChildrenAsync(sessionUpdates);

        // Also update the user's status in the activeUsers node
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("focusMode", true);
        userRef.updateChildrenAsync(userUpdates);
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

    public void sendInvitation(
            String targetUserId,
            String invitationId,
            String fromUserId,
            String fromName,
            String roomId
    ) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference inviteRef = firebaseDatabase
                .getReference("invitations")
                .child(targetUserId)
                .child(invitationId);

        Map<String, Object> inviteData = new HashMap<>();
        inviteData.put("from", fromUserId);
        inviteData.put("fromName", fromName);
        inviteData.put("roomId", roomId);
        inviteData.put("timestamp", System.currentTimeMillis());

        inviteRef.setValueAsync(inviteData);

        // Optional: auto-expire the invite after some time
        inviteRef.onDisconnect().removeValue((error, ref) -> {
            if (error != null) {
                System.err.println("Failed to set onDisconnect for invitation: " + error.getMessage());
            }
        });
    }

    public void joinSharedFocusRoom(
            String focusSessionId,
            String userId,
            String roomId,
            long remainingDuration,
            String userName,
            String userImage
    ) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = firebaseDatabase.getReference("sharedRooms").child(roomId);
        DatabaseReference userRoomRef = roomRef.child(userId);
        DatabaseReference userActiveRef = firebaseDatabase.getReference("activeUsers").child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("focusSessionId", focusSessionId);
        userData.put("name", userName);
        userData.put("image", userImage);
        userData.put("status", FocusStatus.FOCUSING.name());
        userData.put("startedAt", System.currentTimeMillis());
        userData.put("endsAt", System.currentTimeMillis() + remainingDuration * 1000);

        userRoomRef.setValueAsync(userData);

        // Update the user's status in the activeUsers node
        userActiveRef.child("inSharedRoom").setValueAsync(true);
        userActiveRef.child("sharedRoomId").setValueAsync(roomId);

        // Set onDisconnect for shared room entry
        userRoomRef.onDisconnect().removeValue((error, ref) -> {
            if (error != null) {
                System.err.println("Failed to set onDisconnect for shared room: " + error.getMessage());
            }
        });
    }

    public long countActiveUsersInRoom(String roomId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = firebaseDatabase.getReference("sharedRooms").child(roomId);

        CompletableFuture<Long> future = new CompletableFuture<>();
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.complete(dataSnapshot.getChildrenCount());
                } else {
                    future.complete(0L);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS); // Wait for up to 5 seconds
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public String getSharedRoomIdForUser(String userId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userActiveRef = firebaseDatabase.getReference("activeUsers").child(userId);

        CompletableFuture<String> future = new CompletableFuture<>();
        userActiveRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("inSharedRoom").exists() && dataSnapshot.child("inSharedRoom").getValue(Boolean.class)) {
                    future.complete(dataSnapshot.child("sharedRoomId").getValue(String.class));
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean removeInvitation(String targetUserId, String invitationId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference inviteRef = firebaseDatabase
                .getReference("invitations")
                .child(targetUserId)
                .child(invitationId);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        inviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    inviteRef.removeValueAsync();
                    future.complete(true);
                } else {
                    future.complete(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void leaveSharedFocusRoom(String userId, String roomId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // Remove the user from the shared room
        DatabaseReference roomUserRef = db.getReference("sharedRooms")
                .child(roomId)
                .child(userId);
        roomUserRef.removeValueAsync();

        // Reset user's active status
        DatabaseReference userActiveRef = db.getReference("activeUsers").child(userId);
        userActiveRef.child("inSharedRoom").setValueAsync(false);
        userActiveRef.child("sharedRoomId").removeValueAsync();
    }
}