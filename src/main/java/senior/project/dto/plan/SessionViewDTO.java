package senior.project.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.enums.SessionType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionViewDTO {
    String sessionId;
    String courseCode;
    String topicName;
    String assignmentName;
    LocalDate date;
    String start;
    String end;
    Long duration;
    SessionType type;
    Boolean isScheduled;
    Boolean isCompleted;
}
