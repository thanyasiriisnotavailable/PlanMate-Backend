package senior.project.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class StudyAnalyticsDTO {

    private int totalCompletedFocusSessions;
    private long totalFocusDuration;
    private Map<String, Long> subjectBreakdown;
    private List<FocusSessionDetailDTO> focusSessions;

    @Data
    @Builder
    public static class FocusSessionDetailDTO {
        private String id;
        private String courseName;
        private LocalDateTime focusStart;
        private LocalDateTime focusEnd;
        private long elapsed;
    }
}