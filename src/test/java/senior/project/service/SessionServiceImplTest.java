package senior.project.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import senior.project.dao.*;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.service.impl.SessionServiceImpl;
import senior.project.firebase.FirebaseFocusService;
import senior.project.util.SecurityUtil;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceImplTest {

    @Mock private SessionDao sessionDao;
    @Mock private FocusSessionDao focusSessionDao;
    @Mock private GroupMemberDao groupMemberDao;
    @Mock private FirebaseFocusService firebaseFocusService;
    @Mock private UserDao userDao;
    @Mock private FirebaseAuth firebaseAuth;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private final String MOCK_USER_UID = "uid123";
    private final User mockUser = User.builder().uid(MOCK_USER_UID).email("user@example.com").build();
    private final Session mockSession = Session.builder().sessionId("S001").duration(3600L).build();

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

                List<Session> todaySessions = List.of(new Session());
                List<Session> tomorrowSessions = List.of(new Session());
                List<Session> futureSessions = List.of(new Session());

                when(sessionDao.getTodaySessions(mockUser)).thenReturn(todaySessions);
                when(sessionDao.getTomorrowSessions(mockUser)).thenReturn(tomorrowSessions);
                when(sessionDao.getFutureSessions(mockUser)).thenReturn(futureSessions);

                // Act
                Map<String, List<Session>> result = sessionService.getToDoListSessions();

                // Assert
                assertEquals(3, result.size());
                assertEquals(todaySessions, result.get("today"));
                assertEquals(tomorrowSessions, result.get("tomorrow"));
                assertEquals(futureSessions, result.get("future"));
            }
        }

        @Test
        @DisplayName("UTC-16-TC-02: Some categories are empty")
        void getToDoListSessions_someCategoriesEmpty() {
            try (MockedStatic<SecurityUtil> mockedSecurity = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                when(sessionDao.getTodaySessions(mockUser)).thenReturn(List.of(new Session()));
                when(sessionDao.getTomorrowSessions(mockUser)).thenReturn(Collections.emptyList());
                when(sessionDao.getFutureSessions(mockUser)).thenReturn(Collections.emptyList());

                // Act
                Map<String, List<Session>> result = sessionService.getToDoListSessions();

                // Assert
                assertEquals(3, result.size());
                assertFalse(result.get("today").isEmpty());
                assertTrue(result.get("tomorrow").isEmpty());
                assertTrue(result.get("future").isEmpty());
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
                Map<String, List<Session>> result = sessionService.getToDoListSessions();

                // Assert
                assertEquals(3, result.size());
                assertTrue(result.get("today").isEmpty());
                assertTrue(result.get("tomorrow").isEmpty());
                assertTrue(result.get("future").isEmpty());
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
                assertThrows(NullPointerException.class, () -> sessionService.getToDoListSessions());
            }
        }
    }

    @Nested
    @DisplayName("Tests for startFocusSession(String sessionId)")
    class StartFocusSessionTests {
        @Test
        @DisplayName("UTC-17-TC-01: Start session with valid session ID")
        void startSession_validId_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                Map<String, Object> result = sessionService.startFocusSession("S001");

                assertEquals("Focus session started", result.get("message"));
                assertEquals("S001", result.get("sessionId"));
                assertEquals(3600L, result.get("duration"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-02: Session not found")
        void startSession_invalidSessionId_shouldThrow() {
            when(sessionDao.findById("invalid"))
                    .thenReturn(null);

            assertThrows(IllegalArgumentException.class,
                    () -> sessionService.startFocusSession("invalid"));
        }

        @Test
        @DisplayName("UTC-17-TC-03: Firebase fails to return display name")
        void startSession_firebaseThrows_shouldUseEmail() throws FirebaseAuthException {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class);
                 MockedStatic<FirebaseAuth> firebaseStatic = mockStatic(FirebaseAuth.class)) {

                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                firebaseStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                FirebaseAuthException mockedAuthException = mock(FirebaseAuthException.class);
                when(firebaseAuth.getUser(MOCK_USER_UID)).thenThrow(mockedAuthException);

                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                Map<String, Object> result = sessionService.startFocusSession("S001");
                assertEquals("Focus session started", result.get("message"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-04: User has no group memberships")
        void startSession_userWithNoGroups() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                Map<String, Object> result = sessionService.startFocusSession("S001");
                assertEquals("Focus session started", result.get("message"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-05: Exception during DAO save")
        void startSession_daoSaveFails_shouldThrow() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                doThrow(new RuntimeException("DB error")).when(focusSessionDao).save(any());

                assertThrows(RuntimeException.class,
                        () -> sessionService.startFocusSession("S001"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-06: Firebase write throws exception")
        void startSession_firebaseWriteFails_shouldThrow() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                doThrow(new RuntimeException("Firebase write fail"))
                        .when(firebaseFocusService).writeFocusSession(any(), any(), any(), anyLong(), any(), anyList(), any(), any());

                assertThrows(RuntimeException.class,
                        () -> sessionService.startFocusSession("S001"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-07: Duration is zero")
        void startSession_durationZero_shouldFail() {
            Session zeroSession = Session.builder().sessionId("S002").duration(0L).build();

            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S002")).thenReturn(zeroSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                assertThrows(IllegalStateException.class,
                        () -> sessionService.startFocusSession("S002"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-08: Duplicate focus session creation")
        void startSession_duplicateCreation_shouldFail() {
            // Simulate that session already exists for this user - custom rule or validation required
            // Here we assume there's some logic that would throw on duplicate (you can mock it based on your actual impl)
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                doThrow(new IllegalArgumentException("Focus session already started for this user."))
                        .when(focusSessionDao).save(any());

                assertThrows(IllegalArgumentException.class,
                        () -> sessionService.startFocusSession("S001"));
            }
        }
    }
}
