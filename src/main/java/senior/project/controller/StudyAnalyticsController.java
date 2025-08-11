package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import senior.project.dto.StudyAnalyticsDTO;
import senior.project.enums.Range;
import senior.project.service.StudyAnalyticsService;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class StudyAnalyticsController {

    private final StudyAnalyticsService studyAnalyticsService;

    @GetMapping
    public ResponseEntity<StudyAnalyticsDTO> getAnalytics(
            @RequestParam Range range,       // day, week, month, year
            @RequestParam LocalDate date     // selected date
    ) {
        StudyAnalyticsDTO analytics = studyAnalyticsService.getAnalytics(range.toString(), date);
        return ResponseEntity.ok(analytics);
    }
}
