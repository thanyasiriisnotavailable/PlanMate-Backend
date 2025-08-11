package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.StudyAnalyticsDao;
import senior.project.enums.FocusStatus;
import senior.project.repository.FocusSessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StudyAnalyticsDaoImpl implements StudyAnalyticsDao {
    private final FocusSessionRepository focusSessionRepository;

    @Override
    public int countCompletedSessions(String userUid, String range) {
        return focusSessionRepository.countByUserUidAndStatusAndFocusStartBetween(
                userUid, FocusStatus.COMPLETED, getStartDate(range), LocalDateTime.now());
    }

    @Override
    public long sumFocusDuration(String userUid, String range) {
        return focusSessionRepository.sumElapsedSeconds(
                userUid, FocusStatus.COMPLETED, getStartDate(range), LocalDateTime.now());
    }

    @Override
    public Map<LocalDate, Long> getDailyFocusDurations(String userUid, String range) {
        return focusSessionRepository.groupDailyFocusDurations(
                userUid, FocusStatus.COMPLETED, getStartDate(range), LocalDateTime.now()
        ).stream().collect(Collectors.toMap(
                row -> ((java.sql.Date) row[0]).toLocalDate(),
                row -> (Long) row[1]
        ));
    }

    @Override
    public Map<String, Long> getSubjectBreakdown(String userUid, String range) {
        return focusSessionRepository.groupByCourseName(
                userUid, FocusStatus.COMPLETED, getStartDate(range), LocalDateTime.now()
        ).stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
        ));
    }

    private LocalDateTime getStartDate(String range) {
        LocalDate today = LocalDate.now();
        return switch (range.toLowerCase()) {
            case "day" -> today.atStartOfDay();
            case "week" -> today.minusDays(6).atStartOfDay();
            case "month" -> today.withDayOfMonth(1).atStartOfDay();
            case "year" -> today.withDayOfYear(1).atStartOfDay();
            default -> throw new IllegalArgumentException("Invalid range: " + range);
        };
    }
}
