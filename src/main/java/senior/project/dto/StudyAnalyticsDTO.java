package senior.project.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class StudyAnalyticsDTO {
    private int totalCompletedFocusSessions;
    private long totalFocusDuration; // in seconds
    private Map<LocalDate, Long> dailyFocusDurations; // date → seconds
    private Map<String, Long> subjectBreakdown; // course name → seconds
}
