package senior.project.service;

import senior.project.dto.StudyAnalyticsDTO;

import java.time.LocalDate;

public interface StudyAnalyticsService {
    StudyAnalyticsDTO getAnalytics(String range, LocalDate date);
}
