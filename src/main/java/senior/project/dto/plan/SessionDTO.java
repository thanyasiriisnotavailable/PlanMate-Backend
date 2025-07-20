package senior.project.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.enums.SessionType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDTO {
    private String sessionId;
    private Long courseId;
    private Long duration;
    private SessionType type;
    private Boolean isScheduled;
    private Integer sessionNumber;
    private Integer totalSessionsInGroup;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String start;
    private String end;

    private String topicId;
    private String assignmentId;
}
