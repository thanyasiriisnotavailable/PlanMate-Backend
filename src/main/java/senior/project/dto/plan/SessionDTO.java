package senior.project.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.CourseId;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDTO {
    private String sessionId;
    private CourseId courseId;
    private String topicId;
    private Date date;
    private String start;
    private String end;
    private Long durationMinutes;
    private String type;
    private Boolean completed;
}
