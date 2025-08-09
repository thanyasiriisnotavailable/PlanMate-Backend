package senior.project.dto;

import lombok.*;
import senior.project.dto.plan.SessionDTO;
import senior.project.enums.FocusStatus;
import senior.project.enums.SessionType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FocusSessionDTO {
    private String id;
    private SessionDTO session;
    private Long courseId;
    private String topicId;
    private String assignmentId;
    private LocalDateTime focusStart;
    private LocalDateTime focusEnd;
    private Long elapsedSeconds;
    private Long plannedDuration;
    private FocusStatus status;
    private SessionType sessionType;
}
