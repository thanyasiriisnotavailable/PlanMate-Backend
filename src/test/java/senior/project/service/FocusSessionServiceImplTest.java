package senior.project.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import senior.project.dao.FocusSessionDao;
import senior.project.dao.GroupMemberDao;
import senior.project.dao.SessionDao;
import senior.project.dao.UserDao;
import senior.project.entity.FocusSession;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.firebase.FirebaseFocusService;
import senior.project.service.impl.FocusSessionServiceImpl;
import senior.project.util.SecurityUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class FocusSessionServiceImplTest {
    // Add missing DAO mocks
    @Mock private SessionDao sessionDao;
    @Mock private UserDao userDao;

    @Mock private FocusSessionDao focusSessionDao;
    @Mock private GroupMemberDao groupMemberDao;
    @Mock private FirebaseFocusService firebaseFocusService;
    @Mock private FirebaseAuth firebaseAuth;
    @Mock private UserRecord userRecord;

    @InjectMocks
    private FocusSessionServiceImpl focusSessionService;

    private final String MOCK_USER_UID = "uid123";
    private final User mockUser = User.builder().uid(MOCK_USER_UID).email("user@example.com").build();
    private Session mockSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockSession = Session.builder().sessionId("S001").duration(3600L).build();
    }

    @Nested
    @DisplayName("Tests for startFocusSession(String sessionId)")
    class StartFocusSessionTests {
        @Test
        @DisplayName("UTC-17-TC-01: Start session with valid session ID")
        void startSession_validId_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                // Arrange
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                // Act
                Map<String, Object> result = focusSessionService.startFocusSession("S001");

                // Assert
                assertNotNull(result);
                assertEquals("Focus session started", result.get("message"));
                assertEquals("S001", result.get("sessionId"));
                assertEquals(3600L, result.get("duration"));
                verify(focusSessionDao, times(1)).save(any(FocusSession.class));
                verify(firebaseFocusService, times(1)).writeFocusSession(any(), any(), any(), anyLong(), any(), anyList(), any(), any());
            }
        }

        @Test
        @DisplayName("UTC-17-TC-02: Session not found")
        void startSession_invalidSessionId_shouldThrow() {
            // Arrange
            when(sessionDao.findById("invalid")).thenReturn(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> focusSessionService.startFocusSession("invalid"));
        }

        @Test
        @DisplayName("UTC-17-TC-03: Firebase auth fails, should use email as display name")
        void startSession_firebaseAuthThrows_shouldUseEmail() {
            try (MockedStatic<SecurityUtil> mockedUtil = mockStatic(SecurityUtil.class);
                 MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class)) {
                // Arrange
                mockedUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                mockedAuth.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                // Simulate FirebaseAuth throwing an exception
                when(firebaseAuth.getUser(MOCK_USER_UID)).thenThrow(mock(FirebaseAuthException.class));

                ArgumentCaptor<String> displayNameCaptor = ArgumentCaptor.forClass(String.class);

                // Act
                focusSessionService.startFocusSession("S001");

                // Assert: Verify that the fallback display name (email) was used
                verify(firebaseFocusService).writeFocusSession(any(), any(), any(), anyLong(), displayNameCaptor.capture(), anyList(), any(), any());
                assertEquals(mockUser.getEmail(), displayNameCaptor.getValue());
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("UTC-17-TC-05: Exception during DAO save should throw")
        void startSession_daoSaveFails_shouldThrow() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                // Arrange
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                doThrow(new RuntimeException("DB error")).when(focusSessionDao).save(any());

                // Act & Assert
                assertThrows(RuntimeException.class,
                        () -> focusSessionService.startFocusSession("S001"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-06: Firebase write fails should NOT throw")
        void startSession_firebaseWriteFails_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                // Arrange
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());

                // Simulate Firebase write failure
                doThrow(new RuntimeException("Firebase write fail"))
                        .when(firebaseFocusService).writeFocusSession(any(), any(), any(), anyLong(), any(), anyList(), any(), any());

                // Act & Assert: The method should complete without throwing an exception
                Map<String, Object> result = assertDoesNotThrow(
                        () -> focusSessionService.startFocusSession("S001")
                );

                // Verify the method still returns the success map
                assertNotNull(result);
                assertEquals("Focus session started", result.get("message"));
            }
        }

        @Test
        @DisplayName("UTC-17-TC-07: Duration is zero should succeed")
        void startSession_durationZero_shouldSucceed() {
            // Arrange
            Session zeroDurationSession = Session.builder().sessionId("S002").duration(0L).build();
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S002")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                // Act
                Map<String, Object> result = focusSessionService.startFocusSession("S002");

                // Assert
                assertNotNull(result);
                assertEquals(0L, result.get("duration"));
            }
        }
    }
}