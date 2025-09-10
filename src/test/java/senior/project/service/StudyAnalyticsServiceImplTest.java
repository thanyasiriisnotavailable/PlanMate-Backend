package senior.project.service;

import com.google.api.pathtemplate.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import senior.project.dao.StudyAnalyticsDao;
import senior.project.dto.StudyAnalyticsDTO;
import senior.project.enums.Range;
import senior.project.service.impl.StudyAnalyticsServiceImpl;
import senior.project.util.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudyAnalyticsServiceImplTest {

    @Mock
    private StudyAnalyticsDao studyAnalyticsDao;

    @InjectMocks
    private StudyAnalyticsServiceImpl studyAnalyticsService;

    private final String MOCK_USER_UID = "uid123";
    private LocalDate today;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        today = LocalDate.now();
    }

    @Nested
    @DisplayName("UTC-23: getAnalytics tests")
    class GetAnalyticsTests {

        @Test
        @DisplayName("UTC-23-TC-01: Get analytics for valid day range")
        void getAnalytics_day_shouldReturnStats() {
            runAnalyticsTest(Range.DAY);
        }

        @Test
        @DisplayName("UTC-23-TC-02: Get analytics for valid week range")
        void getAnalytics_week_shouldReturnStats() {
            runAnalyticsTest(Range.WEEK);
        }

        @Test
        @DisplayName("UTC-23-TC-03: Get analytics for valid month range")
        void getAnalytics_month_shouldReturnStats() {
            runAnalyticsTest(Range.MONTH);
        }

        @Test
        @DisplayName("UTC-23-TC-04: Get analytics for valid year range")
        void getAnalytics_year_shouldReturnStats() {
            runAnalyticsTest(Range.YEAR);
        }

        @Test
        @DisplayName("UTC-23-TC-05: Fail for null range")
        void getAnalytics_nullRange_shouldThrow() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                var ex = assertThrows(ValidationException.class,
                        () -> studyAnalyticsService.getAnalytics(null, today));
                assertEquals("Range must not be null.", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-23-TC-06: Fail for null date")
        void getAnalytics_nullDate_shouldThrow() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                var ex = assertThrows(ValidationException.class,
                        () -> studyAnalyticsService.getAnalytics(Range.DAY, null));
                assertEquals("Date must not be null.", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-23-TC-07: Database error during fetch")
        void getAnalytics_dbError_shouldPropagate() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(studyAnalyticsDao.countCompletedSessions(MOCK_USER_UID, "DAY", today))
                        .thenThrow(new RuntimeException("DB error"));

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> studyAnalyticsService.getAnalytics(Range.DAY, today));
                assertEquals("DB error", ex.getMessage());
            }
        }

        // --------------------
        // Helper method to reduce code repetition
        private void runAnalyticsTest(Range range) {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                // Mock DAO calls
                when(studyAnalyticsDao.countCompletedSessions(MOCK_USER_UID, range.toString(), today)).thenReturn(3);
                when(studyAnalyticsDao.sumFocusDuration(MOCK_USER_UID, range.toString(), today)).thenReturn(7200L);
                when(studyAnalyticsDao.getSubjectBreakdown(MOCK_USER_UID, range.toString(), today))
                        .thenReturn(Map.of("Math", 3600L, "Physics", 3600L));

                @SuppressWarnings("unchecked")
                List<Object[]> mockSessions = (List<Object[]>) (List<?>) List.of(
                        new Object[]{"fs1", "Math", LocalDateTime.now().minusHours(2),
                                LocalDateTime.now().minusHours(1), 3600L},
                        new Object[]{"fs2", "Physics", LocalDateTime.now().minusHours(4),
                                LocalDateTime.now().minusHours(3), 3600L}
                );

                when(studyAnalyticsDao.getCompletedSessionsWithTimes(MOCK_USER_UID, range.toString(), today))
                        .thenReturn(mockSessions);

                // Call service
                StudyAnalyticsDTO result = studyAnalyticsService.getAnalytics(range, today);

                // Assertions
                assertNotNull(result);
                assertEquals(3, result.getTotalCompletedFocusSessions());
                assertEquals(7200L, result.getTotalFocusDuration());
                assertEquals(2, result.getFocusSessions().size());
                assertEquals("fs1", result.getFocusSessions().get(0).getId());
                assertEquals("Math", result.getFocusSessions().get(0).getCourseName());
                assertEquals("fs2", result.getFocusSessions().get(1).getId());
                assertEquals("Physics", result.getFocusSessions().get(1).getCourseName());
                assertEquals(3600L, result.getFocusSessions().get(0).getElapsed());
                assertEquals(3600L, result.getFocusSessions().get(1).getElapsed());
            }
        }
    }
}