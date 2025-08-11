package senior.project.dao;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

public interface StudyAnalyticsDao {
    int countCompletedSessions(String userUid, String range, LocalDate date);
    long sumFocusDuration(String userUid, String range, LocalDate date);
    Map<String, Long> getSubjectBreakdown(String userUid, String range, LocalDate date);
    List<Object[]> getCompletedSessionsWithTimes(String userUid, String range, LocalDate date);
}

