package senior.project.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import senior.project.dao.StudyAnalyticsDao;
import senior.project.enums.FocusStatus;
import senior.project.repository.FocusSessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StudyAnalyticsDaoImpl implements StudyAnalyticsDao {

    private final FocusSessionRepository focusSessionRepository;

    @Override
    public int countCompletedSessions(String userUid, String range, LocalDate date) {
        return focusSessionRepository.countByUserUidAndStatusAndFocusStartBetween(
                userUid,
                FocusStatus.COMPLETED,
                getStartDate(range, date),
                getEndDate(range, date)
        );
    }

    @Override
    public long sumFocusDuration(String userUid, String range, LocalDate date) {
        return focusSessionRepository.sumElapsedSeconds(
                userUid,
                FocusStatus.COMPLETED,
                getStartDate(range, date),
                getEndDate(range, date)
        );
    }

    @Override
    public Map<String, Long> getSubjectBreakdown(String userUid, String range, LocalDate date) {
        return focusSessionRepository.groupByCourseName(
                userUid,
                FocusStatus.COMPLETED,
                getStartDate(range, date),
                getEndDate(range, date)
        ).stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
        ));
    }

    @Override
    public List<Object[]> getCompletedSessionsWithTimes(String userUid, String range, LocalDate date) {
        return focusSessionRepository.findCompletedSessionsWithTimes(
                userUid,
                FocusStatus.COMPLETED,
                getStartDate(range, date),
                getEndDate(range, date)
        );
    }


    private LocalDateTime getStartDate(String range, LocalDate date) {
        return switch (range.toLowerCase()) {
            case "day" -> date.atStartOfDay();
            case "week" -> date.minusDays(date.getDayOfWeek().getValue() - 1L).atStartOfDay(); // Monday start
            case "month" -> date.withDayOfMonth(1).atStartOfDay();
            case "year" -> date.withDayOfYear(1).atStartOfDay();
            default -> throw new IllegalArgumentException("Invalid range: " + range);
        };
    }

    private LocalDateTime getEndDate(String range, LocalDate date) {
        return switch (range.toLowerCase()) {
            case "day" -> date.plusDays(1).atStartOfDay().minusNanos(1);
            case "week" -> date.minusDays(date.getDayOfWeek().getValue() - 1L)
                    .plusDays(7).atStartOfDay().minusNanos(1);
            case "month" -> date.withDayOfMonth(1).plusMonths(1).atStartOfDay().minusNanos(1);
            case "year" -> date.withDayOfYear(1).plusYears(1).atStartOfDay().minusNanos(1);
            default -> throw new IllegalArgumentException("Invalid range: " + range);
        };
    }
}