package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import senior.project.dao.StudyAnalyticsDao;
import senior.project.dto.StudyAnalyticsDTO;
import senior.project.service.StudyAnalyticsService;
import senior.project.util.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyAnalyticsServiceImpl implements StudyAnalyticsService {

    private final StudyAnalyticsDao studyAnalyticsDao;

    @Override
    public StudyAnalyticsDTO getAnalytics(String range) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        int totalSessions = studyAnalyticsDao.countCompletedSessions(userUid, range);
        long totalDuration = studyAnalyticsDao.sumFocusDuration(userUid, range);

        Map<LocalDate, Long> dailyFocus = removeNullKeys(studyAnalyticsDao.getDailyFocusDurations(userUid, range));
        Map<String, Long> subjectBreakdown = removeNullKeys(studyAnalyticsDao.getSubjectBreakdown(userUid, range));

        return StudyAnalyticsDTO.builder()
                .totalCompletedFocusSessions(totalSessions)
                .totalFocusDuration(totalDuration)
                .dailyFocusDurations(dailyFocus)
                .subjectBreakdown(subjectBreakdown)
                .build();
    }

    private <K, V> Map<K, V> removeNullKeys(Map<K, V> map) {
        if (map == null) return Map.of();
        return map.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}