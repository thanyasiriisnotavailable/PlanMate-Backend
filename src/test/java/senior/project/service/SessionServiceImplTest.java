package senior.project.service;

import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.*;
import org.mockito.*;
import senior.project.dao.*;
import senior.project.dto.plan.SessionDTO;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.service.impl.SessionServiceImpl;
import senior.project.firebase.FirebaseFocusService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceImplTest {
    @Mock private SessionDao sessionDao;
    @Mock private UserDao userDao;
    @Mock private DTOMapper dtoMapper;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private final String MOCK_USER_UID = "uid123";
    private final User mockUser = User.builder().uid(MOCK_USER_UID).email("user@example.com").build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Tests for getToDoListSessions()")
    class GetToDoListSessionsTests {

        @Test
        @DisplayName("UTC-16-TC-01: All categories contain sessions")
        void getToDoListSessions_allCategoriesNonEmpty() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                Session todayS = new Session();
                Session tomorrowS = new Session();
                Session upcomingS = new Session();

                List<Session> todaySessions = List.of(todayS);
                List<Session> tomorrowSessions = List.of(tomorrowS);
                List<Session> upcomingSessions = List.of(upcomingS);

                when(sessionDao.getTodaySessions(mockUser)).thenReturn(todaySessions);
                when(sessionDao.getTomorrowSessions(mockUser)).thenReturn(tomorrowSessions);
                when(sessionDao.getFutureSessions(mockUser)).thenReturn(upcomingSessions);

                // Mapper stubs
                SessionDTO todayDto = new SessionDTO();
                SessionDTO tomorrowDto = new SessionDTO();
                SessionDTO upcomingDto = new SessionDTO();

                when(dtoMapper.toSessionDto(todayS)).thenReturn(todayDto);
                when(dtoMapper.toSessionDto(tomorrowS)).thenReturn(tomorrowDto);
                when(dtoMapper.toSessionDto(upcomingS)).thenReturn(upcomingDto);

                // Act
                Map<String, List<SessionDTO>> result = sessionService.getToDoListSessions();

                // Assert
                assertEquals(3, result.size());
                assertEquals(List.of(todayDto), result.get("today"));
                assertEquals(List.of(tomorrowDto), result.get("tomorrow"));
                assertEquals(List.of(upcomingDto), result.get("upcoming"));
            }
        }

        @Test
        @DisplayName("UTC-16-TC-02: Some categories are empty")
        void getToDoListSessions_someCategoriesEmpty() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                Session todayS = new Session();
                when(sessionDao.getTodaySessions(mockUser)).thenReturn(List.of(todayS));
                when(sessionDao.getTomorrowSessions(mockUser)).thenReturn(Collections.emptyList());
                when(sessionDao.getFutureSessions(mockUser)).thenReturn(Collections.emptyList());

                SessionDTO todayDto = new SessionDTO();
                when(dtoMapper.toSessionDto(todayS)).thenReturn(todayDto);

                // Act
                Map<String, List<SessionDTO>> result = sessionService.getToDoListSessions();

                // Assert
                assertEquals(3, result.size());
                assertEquals(List.of(todayDto), result.get("today"));
                assertTrue(result.get("tomorrow").isEmpty());
                assertTrue(result.get("upcoming").isEmpty());
            }
        }

        @Test
        @DisplayName("UTC-16-TC-03: No sessions exist")
        void getToDoListSessions_allEmpty() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                when(sessionDao.getTodaySessions(mockUser)).thenReturn(Collections.emptyList());
                when(sessionDao.getTomorrowSessions(mockUser)).thenReturn(Collections.emptyList());
                when(sessionDao.getFutureSessions(mockUser)).thenReturn(Collections.emptyList());

                // Act
                Map<String, List<SessionDTO>> result = sessionService.getToDoListSessions();

                // Assert
                assertEquals(3, result.size());
                assertTrue(result.get("today").isEmpty());
                assertTrue(result.get("tomorrow").isEmpty());
                assertTrue(result.get("upcoming").isEmpty());
                // No mapper interactions expected for empty lists
                verify(dtoMapper, never()).toSessionDto(any());
            }
        }

        @Test
        @DisplayName("UTC-16-TC-04: User not found – should throw NullPointerException")
        void getToDoListSessions_userNotFound_shouldThrow() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(null); // User not found

                // Act & Assert
                NullPointerException ex = assertThrows(NullPointerException.class,
                        () -> sessionService.getToDoListSessions());
                assertTrue(ex.getMessage().contains(MOCK_USER_UID));
            }
        }
    }
}
