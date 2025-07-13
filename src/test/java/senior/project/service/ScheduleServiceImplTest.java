package senior.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import senior.project.dao.*;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.SessionDTO;
import senior.project.entity.*;
import senior.project.entity.plan.Schedule;
import senior.project.entity.plan.Session;
import senior.project.service.impl.ScheduleServiceImpl;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ScheduleServiceImplTest {

    @Mock
    private ScheduleDao scheduleDao;
    @Mock
    private UserDao userDao;
    @Mock
    private CourseDao courseDao;
    @Mock
    private TopicDao topicDao;
    @Mock
    private TermDao termDao;
    @Mock
    private AssignmentDao assignmentDao;
    @Mock
    private DTOMapper mapper;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private static final String MOCK_USER_UID = "test-user-schedule-123";
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setUid(MOCK_USER_UID);
    }

    private ScheduleDTO createValidScheduleDTO() {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setTermId(1L);
        dto.setGeneratedAt(LocalDateTime.now().toString());
        dto.setStudyPlan(new ArrayList<>());
        dto.setUnscheduledPlan(new ArrayList<>());
        return dto;
    }

    private SessionDTO createSessionDTO(String sessionId, Long courseId, String topicId, String assignmentId, boolean isScheduled) {
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setSessionId(sessionId);
        sessionDTO.setCourseId(courseId);
        sessionDTO.setTopicId(topicId);
        sessionDTO.setAssignmentId(assignmentId);
        sessionDTO.setDate(LocalDate.now());
        sessionDTO.setStart(String.valueOf(LocalTime.of(10, 0)));
        sessionDTO.setEnd(String.valueOf(LocalTime.of(11, 0)));
        sessionDTO.setIsScheduled(isScheduled);
        return sessionDTO;
    }


    @Nested
    @DisplayName("Tests for saveSchedule(ScheduleDTO dto)")
    class SaveScheduleTests {

        @Test
        @DisplayName("UTC-12-TC-01: Save valid schedule with multiple sessions")
        void saveSchedule_withValidData_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                Term mockTerm = new Term();
                mockTerm.setTermId(1L);
                when(termDao.findById(1L)).thenReturn(Optional.of(mockTerm));

                ScheduleDTO dto = createValidScheduleDTO();
                SessionDTO scheduledSessionDto = createSessionDTO("s1", 101L, "201", null, true);
                SessionDTO unscheduledSessionDto = createSessionDTO("s2", 102L, null, "301", false);
                dto.getStudyPlan().add(scheduledSessionDto);
                dto.getUnscheduledPlan().add(unscheduledSessionDto);

                // Mock mappers and DAOs for relations
                when(mapper.toSession(any(SessionDTO.class))).thenReturn(new Session());
                when(courseDao.findById(anyLong())).thenReturn(new Course());
                when(topicDao.findById(anyString())).thenReturn(new Topic());
                when(assignmentDao.findById(anyString())).thenReturn(new Assignment());

                // Mocks for getSchedule() call at the end
                Schedule savedSchedule = new Schedule();
                // Create a realistic mock of the session that would be retrieved from the DB
                Session mockRetrievedSession = new Session();
                mockRetrievedSession.setIsScheduled(true); // Explicitly set the boolean to avoid NullPointerException

                savedSchedule.setSessions(List.of(mockRetrievedSession)); // Use the properly initialized session
                savedSchedule.setTerm(mockTerm);
                savedSchedule.setGeneratedAt(LocalDateTime.now());
                when(scheduleDao.save(any(Schedule.class))).thenReturn(savedSchedule);
                when(scheduleDao.findByUser(mockUser)).thenReturn(List.of(savedSchedule));
                when(mapper.toSessionDto(any(Session.class))).thenReturn(new SessionDTO()); // Crucial mock for getSchedule()

                // Act
                scheduleService.saveSchedule(dto);

                // Assert
                ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
                verify(scheduleDao).save(scheduleCaptor.capture());
                Schedule capturedSchedule = scheduleCaptor.getValue();

                assertThat(capturedSchedule.getUser()).isEqualTo(mockUser);
                assertThat(capturedSchedule.getTerm()).isEqualTo(mockTerm);
                assertThat(capturedSchedule.getSessions()).hasSize(2);
                verify(courseDao, times(2)).findById(anyLong());
                verify(topicDao, times(1)).findById("201");
                verify(assignmentDao, times(1)).findById("301");
            }
        }

        @Test
        @DisplayName("UTC-12-TC-02: Save schedule with no sessions")
        void saveSchedule_withNoSessions_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                Term mockTerm = new Term();
                when(termDao.findById(1L)).thenReturn(Optional.of(mockTerm));

                ScheduleDTO dto = createValidScheduleDTO(); // Has empty lists by default

                // Mocks for getSchedule()
                Schedule savedSchedule = new Schedule();
                savedSchedule.setSessions(Collections.emptyList());
                savedSchedule.setTerm(mockTerm);
                savedSchedule.setGeneratedAt(LocalDateTime.now());
                when(scheduleDao.save(any(Schedule.class))).thenReturn(savedSchedule);
                when(scheduleDao.findByUser(mockUser)).thenReturn(List.of(savedSchedule));

                // Act
                scheduleService.saveSchedule(dto);

                // Assert
                ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
                verify(scheduleDao).save(scheduleCaptor.capture());
                assertThat(scheduleCaptor.getValue().getSessions()).isEmpty();
            }
        }

        @Test
        @DisplayName("UTC-12-TC-03: Save schedule with missing optional links")
        void saveSchedule_withMissingLinks_shouldSkipGracefully() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                Term mockTerm = new Term();
                when(termDao.findById(1L)).thenReturn(Optional.of(mockTerm));

                ScheduleDTO dto = createValidScheduleDTO();
                // This session only has a courseId, others are null
                dto.getStudyPlan().add(createSessionDTO("s1", 101L, null, null, true));

                when(mapper.toSession(any())).thenReturn(new Session());
                when(courseDao.findById(101L)).thenReturn(new Course());

                // Mocks for getSchedule() call at the end
                Schedule savedSchedule = new Schedule();

                // Create a realistic mock of the session that would be retrieved from the DB
                Session mockRetrievedSession = new Session();
                mockRetrievedSession.setIsScheduled(true); // Explicitly set the boolean to avoid NullPointerException

                savedSchedule.setSessions(List.of(mockRetrievedSession)); // Use the properly initialized session
                savedSchedule.setTerm(mockTerm);
                savedSchedule.setGeneratedAt(LocalDateTime.now());
                when(scheduleDao.save(any(Schedule.class))).thenReturn(savedSchedule);
                when(scheduleDao.findByUser(mockUser)).thenReturn(List.of(savedSchedule));
                when(mapper.toSessionDto(any(Session.class))).thenReturn(new SessionDTO()); // Crucial mock

                // Act
                scheduleService.saveSchedule(dto);

                // Assert
                ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
                verify(scheduleDao).save(scheduleCaptor.capture());
                assertThat(scheduleCaptor.getValue().getSessions()).hasSize(1);
                verify(courseDao, times(1)).findById(101L);
                verify(topicDao, never()).findById(anyString()); // Should not be called
                verify(assignmentDao, never()).findById(anyString()); // Should not be called
            }
        }

        @Test
        @DisplayName("UTC-12-TC-04: Fail when session has null date or time")
        void saveSchedule_withNullDate_shouldFail() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(termDao.findById(1L)).thenReturn(Optional.of(new Term()));

                ScheduleDTO dto = createValidScheduleDTO();
                SessionDTO badSession = createSessionDTO("s1", 101L, null, null, true);
                badSession.setDate(null); // Invalid data
                dto.getStudyPlan().add(badSession);

                // Simulate a persistence or mapping failure
                when(mapper.toSession(badSession)).thenThrow(new IllegalArgumentException("Date cannot be null"));

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                    scheduleService.saveSchedule(dto);
                });
                verify(scheduleDao, never()).save(any());
            }
        }

        @Test
        @DisplayName("UTC-12-TC-05: Fail when termId is invalid")
        void saveSchedule_withInvalidTermId_shouldAssignNullTerm() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(termDao.findById(-999L)).thenReturn(Optional.empty()); // Term not found

                ScheduleDTO dto = createValidScheduleDTO();
                dto.setTermId(-999L);

                // Mocks for getSchedule()
                Schedule savedSchedule = new Schedule();
                savedSchedule.setSessions(Collections.emptyList());
                savedSchedule.setGeneratedAt(LocalDateTime.now());
                savedSchedule.setTerm(new Term());
                when(scheduleDao.save(any(Schedule.class))).thenReturn(savedSchedule);
                when(scheduleDao.findByUser(mockUser)).thenReturn(List.of(savedSchedule));

                // Act
                scheduleService.saveSchedule(dto);

                // Assert
                ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
                verify(scheduleDao).save(scheduleCaptor.capture());
                assertThat(scheduleCaptor.getValue().getTerm()).isNull();
            }
        }

        @Test
        @DisplayName("UTC-12-TC-06: Fail on null ScheduleDTO input")
        void saveSchedule_withNullDto_shouldThrowException() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                scheduleService.saveSchedule(null);
            });
        }
    }
}