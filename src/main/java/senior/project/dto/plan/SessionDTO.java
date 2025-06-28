package senior.project.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDTO {
    private String sessionId;
    private Long courseId;
    private String topicId;
    private Date date;
    private String start;
    private String end;
    private Long durationMinutes;
    private String type;
    private Boolean completed;
}
