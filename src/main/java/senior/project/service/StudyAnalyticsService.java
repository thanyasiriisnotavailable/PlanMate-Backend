package senior.project.service;

import senior.project.dto.StudyAnalyticsDTO;
import senior.project.enums.Range;

import java.time.LocalDate;

public interface StudyAnalyticsService {
    StudyAnalyticsDTO getAnalytics(Range range, LocalDate date);
}
