package senior.project.dao;

import java.time.LocalDate;
import java.util.Map;

public interface StudyAnalyticsDao {
    int countCompletedSessions(String userId, String range);
    long sumFocusDuration(String userId, String range);
    Map<LocalDate, Long> getDailyFocusDurations(String userId, String range);
    Map<String, Long> getSubjectBreakdown(String userId, String range);
}

