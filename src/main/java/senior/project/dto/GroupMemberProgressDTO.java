package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberProgressDTO {
    private MemberProfileDTO member;
    private int completedSessions;
    private long totalFocusSeconds;
    private double percentageCompleted;
    private double points;
}