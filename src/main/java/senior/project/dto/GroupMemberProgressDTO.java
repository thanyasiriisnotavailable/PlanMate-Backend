package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberProgressDTO {
    private String userUid;
    private int completedSessions;
    private long totalFocusSeconds;
    private double totalScore;
}