package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.StudyAnalyticsDao;
import senior.project.dto.StudyAnalyticsDTO;
import senior.project.service.StudyAnalyticsService;
import senior.project.util.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyAnalyticsServiceImpl implements StudyAnalyticsService {

    private final StudyAnalyticsDao studyAnalyticsDao;

    @Override
    public StudyAnalyticsDTO getAnalytics(String range, LocalDate date) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        int totalSessions = studyAnalyticsDao.countCompletedSessions(userUid, range, date);
        long totalDuration = studyAnalyticsDao.sumFocusDuration(userUid, range, date);
        Map<String, Long> subjectBreakdown = removeNullKeys(studyAnalyticsDao.getSubjectBreakdown(userUid, range, date));

        List<StudyAnalyticsDTO.FocusSessionDetailDTO> sessionDetails =
                studyAnalyticsDao.getCompletedSessionsWithTimes(userUid, range, date).stream()
                        .map(row -> StudyAnalyticsDTO.FocusSessionDetailDTO.builder()
                                .id((String) row[0])
                                .courseName((String) row[1])
                                .focusStart((LocalDateTime) row[2])
                                .focusEnd((LocalDateTime) row[3])
                                .elapsed((Long) row[4])
                                .build()
                        )
                        .toList();

        return StudyAnalyticsDTO.builder()
                .totalCompletedFocusSessions(totalSessions)
                .totalFocusDuration(totalDuration)
                .subjectBreakdown(subjectBreakdown)
                .focusSessions(sessionDetails)
                .build();

    }

    private <K, V> Map<K, V> removeNullKeys(Map<K, V> map) {
        if (map == null) return Map.of();
        return map.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}