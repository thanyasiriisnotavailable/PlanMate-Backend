package senior.project.service;

import com.google.api.pathtemplate.ValidationException;
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
import senior.project.dto.FocusSessionDTO;
import senior.project.entity.FocusSession;
import senior.project.entity.User;
import senior.project.entity.plan.Session;
import senior.project.enums.FocusStatus;
import senior.project.firebase.FirebaseFocusService;
import senior.project.service.impl.FocusSessionServiceImpl;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FocusSessionServiceImplTest {
    @Mock private SessionDao sessionDao;
    @Mock private UserDao userDao;

    @Mock private FocusSessionDao focusSessionDao;
    @Mock private GroupMemberDao groupMemberDao;
    @Mock private FirebaseFocusService firebaseFocusService;
    @Mock private FirebaseAuth firebaseAuth;
    @Mock private UserRecord userRecord;
    @Mock private DTOMapper dtoMapper;

    @Spy
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
        // Helper method to set up the mocking for successful `save`
        private void setupMockSave() {
            when(focusSessionDao.save(any(FocusSession.class))).thenAnswer(invocation -> {
                FocusSession savedSession = invocation.getArgument(0);
                savedSession.setId(UUID.randomUUID().toString());
                return savedSession;
            });
        }

        @Test
        @DisplayName("UTC-17-TC-01: Start session with valid session ID")
        void startSession_validId_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                // Arrange
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());
                // No active focus session
                doReturn(null).when(focusSessionService).getActiveFocusSessionForUser(MOCK_USER_UID);

                setupMockSave();

                // Act
                Map<String, Object> result = focusSessionService.startFocusSession("S001");

                // Assert
                assertNotNull(result);
                assertEquals("Focus session started", result.get("message"));
                assertEquals("S001", result.get("sessionId"));
                assertEquals(3600L, result.get("duration"));
                assertNotNull(result.get("focusSessionId"));
                verify(focusSessionDao, times(1)).save(any(FocusSession.class));
                verify(firebaseFocusService, times(1))
                        .writeFocusSession(any(), any(), any(), anyLong(), any(), any(), anyList(), any(), any());
            }
        }

        @Test
        @DisplayName("UTC-17-TC-02: Session not found")
        void startSession_invalidSessionId_shouldThrowValidationException() {
            // Arrange
            when(sessionDao.findById("invalid")).thenReturn(null);

            // Act & Assert
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.startFocusSession("invalid"));
            assertEquals("Session not found or unauthorized.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-17-TC-03: User has no group memberships")
        void startSession_noGroupMemberships_shouldReturnEmptyGroupList() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                // user has no groups
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());
                doReturn(null).when(focusSessionService).getActiveFocusSessionForUser(MOCK_USER_UID);

                setupMockSave();

                Map<String, Object> result = focusSessionService.startFocusSession("S001");

                assertNotNull(result);
                assertTrue(((List<?>) result.get("groupIds")).isEmpty(), "groupIds should be an empty list");
            }
        }

        @Test
        @DisplayName("UTC-17-TC-04: Exception during DAO save should throw")
        void startSession_daoSaveFails_shouldThrow() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                // Arrange
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                doReturn(null).when(focusSessionService).getActiveFocusSessionForUser(MOCK_USER_UID);

                doThrow(new RuntimeException("DB error")).when(focusSessionDao).save(any());

                // Act & Assert
                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> focusSessionService.startFocusSession("S001"));
                assertEquals("DB error", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-17-TC-05: Firebase write throws exception (should propagate)")
        void startSession_firebaseWriteThrows_shouldPropagate() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                // Arrange
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.findByUser(mockUser)).thenReturn(Collections.emptyList());
                doReturn(null).when(focusSessionService).getActiveFocusSessionForUser(MOCK_USER_UID);

                setupMockSave();

                // Simulate firebase write failure AFTER save
                doThrow(new RuntimeException("Firebase error"))
                        .when(firebaseFocusService)
                        .writeFocusSession(any(), any(), any(), anyLong(), any(), any(), anyList(), any(), any());

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> focusSessionService.startFocusSession("S001"));
                assertEquals("Firebase error", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-17-TC-06: Duration is zero should throw ValidationException")
        void startSession_durationZero_shouldThrowValidationException() {
            // Arrange: session with zero duration
            Session zeroDurationSession = Session.builder().sessionId("S002").duration(0L).build();
            when(sessionDao.findById("S002")).thenReturn(zeroDurationSession);

            // Act & Assert
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.startFocusSession("S002"));
            assertEquals("Focus duration must be greater than zero.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-17-TC-07: Duplicate focus session creation should throw ValidationException")
        void startSession_duplicateActiveSession_shouldThrowValidationException() {
            try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(sessionDao.findById("S001")).thenReturn(mockSession);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                // Simulate existing active focus session
                FocusSessionDTO activeMock = mock(FocusSessionDTO.class);
                doReturn(activeMock).when(focusSessionService).getActiveFocusSessionForUser(MOCK_USER_UID);

                ValidationException ex = assertThrows(ValidationException.class,
                        () -> focusSessionService.startFocusSession("S001"));
                // match the message thrown by your service implementation
                assertEquals("You already have an active focus session.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Tests for pauseFocusSession(String focusSessionId)")
    class PauseFocusSessionTests {

        private FocusSession activeSession;

        @BeforeEach
        void setupSession() {
            // Active FOCUSING session mock
            activeSession = FocusSession.builder()
                    .id("fs-123")
                    .status(FocusStatus.FOCUSING)
                    .elapsedSeconds(120L)
                    .focusStart(LocalDateTime.now().minusMinutes(2))
                    .user(mockUser)
                    .session(mockSession)
                    .build();

            // DTO mapper mock (outer class dtoMapper is injected into service)
            when(dtoMapper.toFocusSessionDto(any(FocusSession.class))).thenAnswer(invocation -> {
                FocusSession fs = invocation.getArgument(0);
                FocusSessionDTO dto = new FocusSessionDTO();
                dto.setId(fs.getId());
                dto.setStatus(fs.getStatus());
                dto.setElapsedSeconds(fs.getElapsedSeconds());
                return dto;
            });
        }

        private void setupMockSave() {
            when(focusSessionDao.save(any(FocusSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("UTC-18-TC-01: Pause valid active focus session")
        void pause_validSession_shouldReturnPaused() {
            when(focusSessionDao.findById("fs-123")).thenReturn(activeSession);
            setupMockSave();

            FocusSessionDTO result = focusSessionService.pauseFocusSession("fs-123");

            assertNotNull(result);
            assertEquals(FocusStatus.PAUSED, result.getStatus());
            assertTrue(result.getElapsedSeconds() >= 120L);

            verify(focusSessionDao).save(activeSession);
            verify(firebaseFocusService).updateFocusSession(
                    eq(activeSession.getId()),
                    eq(activeSession.getUser().getUid()),
                    eq(FocusStatus.PAUSED),
                    anyLong()
            );
        }

        @Test
        @DisplayName("UTC-18-TC-02: Session does not exist")
        void pause_nonExistentSession_shouldThrow() {
            when(focusSessionDao.findById("invalid")).thenReturn(null);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.pauseFocusSession("invalid"));
            assertEquals("Session is not active or not found.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-18-TC-03: Session not in FOCUSING status")
        void pause_sessionNotFocusing_shouldThrow() {
            FocusSession pausedSession = FocusSession.builder()
                    .id("fs-124")
                    .status(FocusStatus.PAUSED)
                    .elapsedSeconds(100L)
                    .focusStart(LocalDateTime.now().minusMinutes(1))
                    .build();
            when(focusSessionDao.findById("fs-124")).thenReturn(pausedSession);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.pauseFocusSession("fs-124"));
            assertEquals("Session is not active or not found.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-18-TC-04: Firebase sync failure while pausing session")
        void pause_firebaseFails_shouldStillReturnPaused() {
            when(focusSessionDao.findById("fs-789")).thenReturn(activeSession);
            setupMockSave();

            // Simulate Firebase failure
            doThrow(new RuntimeException("Firebase error"))
                    .when(firebaseFocusService).writeFocusSession(
                            eq(activeSession.getId()),
                            eq(activeSession.getUser().getUid()),
                            eq(activeSession.getSession().getSessionId()),
                            anyLong(),
                            any(),
                            any(),
                            anyList(),
                            eq(activeSession.getFocusStart()),
                            eq(FocusStatus.PAUSED)
                    );

            FocusSessionDTO result = assertDoesNotThrow(() ->
                    focusSessionService.pauseFocusSession("fs-789")
            );

            assertNotNull(result);
            assertEquals(FocusStatus.PAUSED, result.getStatus());
            verify(focusSessionDao).save(activeSession);
        }

        @Test
        @DisplayName("UTC-18-TC-05: Database error during save")
        void pause_dbSaveFails_shouldPropagate() {
            when(focusSessionDao.findById("fs-500")).thenReturn(activeSession);
            doThrow(new RuntimeException("DB error")).when(focusSessionDao).save(any());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> focusSessionService.pauseFocusSession("fs-500"));
            assertEquals("DB error", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for resumeFocusSession(String focusSessionId)")
    class ResumeFocusSessionTests {

        private FocusSession pausedSession;

        @BeforeEach
        void setupPausedSession() {
            pausedSession = FocusSession.builder()
                    .id("fs-123")
                    .status(FocusStatus.PAUSED)
                    .elapsedSeconds(120L)
                    .plannedDuration(3600L) // <-- ensure plannedDuration is set
                    .focusStart(LocalDateTime.now().minusMinutes(2))
                    .user(mockUser)
                    .session(mockSession)
                    .build();

            // Mock the mapper
            when(dtoMapper.toFocusSessionDto(any(FocusSession.class))).thenAnswer(invocation -> {
                FocusSession fs = invocation.getArgument(0);
                FocusSessionDTO dto = new FocusSessionDTO();
                dto.setId(fs.getId());
                dto.setStatus(fs.getStatus());
                dto.setElapsedSeconds(fs.getElapsedSeconds());
                return dto;
            });
        }

        @Test
        @DisplayName("UTC-19-TC-01: Resume valid paused focus session")
        void resume_validPausedSession_shouldReturnFocusing() {
            when(focusSessionDao.findById("fs-123")).thenReturn(pausedSession);
            when(focusSessionDao.save(any(FocusSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FocusSessionDTO result = focusSessionService.resumeFocusSession("fs-123");

            assertNotNull(result);
            assertEquals(FocusStatus.FOCUSING, result.getStatus());
            verify(focusSessionDao).save(pausedSession);

            // Verify the correct Firebase method is called
            verify(firebaseFocusService).resumeFocusSession(
                    eq(pausedSession.getId()),
                    eq(pausedSession.getUser().getUid()),
                    eq(FocusStatus.FOCUSING),
                    any(LocalDateTime.class),
                    eq(pausedSession.getPlannedDuration())
            );
        }

        @Test
        @DisplayName("UTC-19-TC-02: Session does not exist")
        void resume_nonExistentSession_shouldThrow() {
            when(focusSessionDao.findById("invalid")).thenReturn(null);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.resumeFocusSession("invalid"));
            assertEquals("Session is not in a pausable state.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-19-TC-03: Session not in PAUSED status")
        void resume_sessionNotPaused_shouldThrow() {
            FocusSession focusingSession = FocusSession.builder()
                    .id("fs-456")
                    .status(FocusStatus.FOCUSING)
                    .elapsedSeconds(100L)
                    .plannedDuration(3600L)
                    .user(mockUser)
                    .session(mockSession)
                    .build();

            when(focusSessionDao.findById("fs-456")).thenReturn(focusingSession);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.resumeFocusSession("fs-456"));
            assertEquals("Session is not in a pausable state.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-19-TC-04: Firebase sync failure while resuming session")
        void resume_firebaseFails_shouldStillReturnFocusing() {
            when(focusSessionDao.findById("fs-789")).thenReturn(pausedSession);
            when(focusSessionDao.save(any(FocusSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

            doThrow(new RuntimeException("Firebase error"))
                    .when(firebaseFocusService).updateFocusSession(
                            eq(pausedSession.getId()),
                            eq(pausedSession.getUser().getUid()),
                            eq(FocusStatus.FOCUSING),
                            anyLong()
                    );

            FocusSessionDTO result = assertDoesNotThrow(() ->
                    focusSessionService.resumeFocusSession("fs-789")
            );

            assertNotNull(result);
            assertEquals(FocusStatus.FOCUSING, result.getStatus());
            verify(focusSessionDao).save(pausedSession);
        }

        @Test
        @DisplayName("UTC-19-TC-05: Database error during save")
        void resume_dbSaveFails_shouldPropagate() {
            when(focusSessionDao.findById("fs-500")).thenReturn(pausedSession);
            doThrow(new RuntimeException("DB error")).when(focusSessionDao).save(any());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> focusSessionService.resumeFocusSession("fs-500"));
            assertEquals("DB error", ex.getMessage());
        }
    }


    @Nested
    @DisplayName("Tests for endFocusSession(String focusSessionId)")
    class EndFocusSessionTests {

        private FocusSession activeSession;

        @BeforeEach
        void setupActiveSession() {
            activeSession = FocusSession.builder()
                    .id("fs-456")
                    .status(FocusStatus.FOCUSING)
                    .elapsedSeconds(600L)
                    .focusStart(LocalDateTime.now().minusMinutes(10))
                    .user(mockUser)
                    .session(mockSession)
                    .build();

            when(dtoMapper.toFocusSessionDto(any(FocusSession.class))).thenAnswer(invocation -> {
                FocusSession fs = invocation.getArgument(0);
                FocusSessionDTO dto = new FocusSessionDTO();
                dto.setId(fs.getId());
                dto.setStatus(fs.getStatus());
                dto.setElapsedSeconds(fs.getElapsedSeconds());
                return dto;
            });
        }

        private void setupMockSave() {
            when(focusSessionDao.save(any(FocusSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("UTC-20-TC-01: End focus session with valid ID")
        void end_validSession_shouldReturnEnded() {
            when(focusSessionDao.findById("fs-456")).thenReturn(activeSession);
            setupMockSave();

            FocusSessionDTO result = focusSessionService.endFocusSession("fs-456");

            assertNotNull(result);
            assertEquals(FocusStatus.COMPLETED, result.getStatus());
            assertTrue(result.getElapsedSeconds() >= 600L);
            verify(focusSessionDao).save(activeSession);
        }

        @Test
        @DisplayName("UTC-20-TC-02: Fail to end session with invalid ID")
        void end_invalidSession_shouldThrowValidation() {
            when(focusSessionDao.findById("fs-999")).thenReturn(null);

            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> focusSessionService.endFocusSession("fs-999"));
            assertEquals("Focus session not found.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-20-TC-03: Fail to end session already ended")
        void end_alreadyEnded_shouldThrowValidation() {
            FocusSession endedSession = FocusSession.builder()
                    .id("fs-123")
                    .status(FocusStatus.COMPLETED)
                    .elapsedSeconds(600L)
                    .build();
            when(focusSessionDao.findById("fs-123")).thenReturn(endedSession);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.endFocusSession("fs-123"));
            assertEquals("This session has already been completed.", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-20-TC-04: Focus duration too short")
        void end_durationTooShort_shouldThrowValidation() {
            FocusSession shortSession = FocusSession.builder()
                    .id("fs-123")
                    .status(FocusStatus.FOCUSING)
                    .elapsedSeconds(100L) // < 5 minutes
                    .focusStart(LocalDateTime.now().minusMinutes(2))
                    .user(mockUser)
                    .build();
            when(focusSessionDao.findById("fs-123")).thenReturn(shortSession);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> focusSessionService.endFocusSession("fs-123"));
            assertEquals("Focus session is too short. Minimum duration is 5 minutes.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests for joinSharedFocusRoom(String roomId)")
    class JoinSharedRoomTests {

        private FocusSession activeSession;

        @BeforeEach
        void setupActiveSession() {
            activeSession = FocusSession.builder()
                    .id("fs-join-123")
                    .status(FocusStatus.FOCUSING)
                    .elapsedSeconds(300L)
                    .plannedDuration(1200L)
                    .user(mockUser)
                    .session(mockSession)
                    .build();
        }

        @Test
        @DisplayName("UTC-22-TC-01: Join valid room with active session")
        void join_validRoom_shouldSucceed() throws Exception {
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class);
                 MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class)) {

                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(focusSessionDao.findByUserUidAndStatus(MOCK_USER_UID, FocusStatus.FOCUSING))
                        .thenReturn(activeSession);
                when(firebaseFocusService.getSharedRoomIdForUser(MOCK_USER_UID)).thenReturn(null);
                when(firebaseFocusService.countActiveUsersInRoom("room-123")).thenReturn(2L);

                // Mock FirebaseAuth + UserRecord
                FirebaseAuth mockAuth = mock(FirebaseAuth.class);
                UserRecord mockRecord = mock(UserRecord.class);
                mockedAuth.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
                when(mockAuth.getUser(MOCK_USER_UID)).thenReturn(mockRecord);
                when(mockRecord.getDisplayName()).thenReturn("Test User");
                when(mockRecord.getPhotoUrl()).thenReturn("http://test-image");

                Map<String, Object> result = focusSessionService.joinSharedFocusRoom("room-123");

                assertNotNull(result);
                assertEquals("Successfully joined shared focus room", result.get("message"));
                assertEquals("room-123", result.get("roomId"));

                verify(firebaseFocusService).joinSharedFocusRoom(
                        eq(activeSession.getId()),
                        eq(MOCK_USER_UID),
                        eq("room-123"),
                        anyLong(),
                        eq("Test User"),
                        eq("http://test-image")
                );
            }
        }

        @Test
        @DisplayName("UTC-22-TC-02: Fail when user has no active focus session")
        void join_noActiveSession_shouldThrow() {
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(focusSessionDao.findByUserUidAndStatus(MOCK_USER_UID, FocusStatus.FOCUSING))
                        .thenReturn(null);

                ValidationException ex = assertThrows(ValidationException.class,
                        () -> focusSessionService.joinSharedFocusRoom("room-123"));

                assertEquals("You must have an active focus session to join a shared room.", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-22-TC-03: Fail when user already in another shared room")
        void join_alreadyInAnotherRoom_shouldThrow() {
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(focusSessionDao.findByUserUidAndStatus(MOCK_USER_UID, FocusStatus.FOCUSING))
                        .thenReturn(activeSession);
                when(firebaseFocusService.getSharedRoomIdForUser(MOCK_USER_UID)).thenReturn("room-abc");

                ValidationException ex = assertThrows(ValidationException.class,
                        () -> focusSessionService.joinSharedFocusRoom("room-456"));

                assertEquals("You are already in a shared room with ID: room-abc", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-22-TC-04: Fail when room is full (≥5 members)")
        void join_roomFull_shouldThrow() {
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(focusSessionDao.findByUserUidAndStatus(MOCK_USER_UID, FocusStatus.FOCUSING))
                        .thenReturn(activeSession);
                when(firebaseFocusService.getSharedRoomIdForUser(MOCK_USER_UID)).thenReturn(null);
                when(firebaseFocusService.countActiveUsersInRoom("room-789")).thenReturn(5L);

                ValidationException ex = assertThrows(ValidationException.class,
                        () -> focusSessionService.joinSharedFocusRoom("room-789"));

                assertEquals("The shared focus room has reached the maximum of 5 members.", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-22-TC-05: Firebase sync failure while joining")
        void join_firebaseFails_shouldPropagate() throws Exception {
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class);
                 MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class)) {

                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(focusSessionDao.findByUserUidAndStatus(MOCK_USER_UID, FocusStatus.FOCUSING))
                        .thenReturn(activeSession);
                when(firebaseFocusService.getSharedRoomIdForUser(MOCK_USER_UID)).thenReturn(null);
                when(firebaseFocusService.countActiveUsersInRoom("room-321")).thenReturn(1L);

                FirebaseAuth mockAuth = mock(FirebaseAuth.class);
                UserRecord mockRecord = mock(UserRecord.class);
                mockedAuth.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
                when(mockAuth.getUser(MOCK_USER_UID)).thenReturn(mockRecord);

                doThrow(new RuntimeException("Firebase join error"))
                        .when(firebaseFocusService).joinSharedFocusRoom(
                                any(), any(), any(), anyLong(), any(), any()
                        );

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> focusSessionService.joinSharedFocusRoom("room-321"));

                assertEquals("Failed to join shared session on Firebase.", ex.getMessage());
            }
        }

        @Test
        @DisplayName("UTC-22-TC-06: Invalid room (does not exist → treated as empty)")
        void join_invalidRoom_shouldSucceedAsEmpty() throws Exception {
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class);
                 MockedStatic<FirebaseAuth> mockedAuth = mockStatic(FirebaseAuth.class)) {

                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(focusSessionDao.findByUserUidAndStatus(MOCK_USER_UID, FocusStatus.FOCUSING))
                        .thenReturn(activeSession);
                when(firebaseFocusService.getSharedRoomIdForUser(MOCK_USER_UID)).thenReturn(null);
                when(firebaseFocusService.countActiveUsersInRoom("room-999")).thenReturn(0L);

                FirebaseAuth mockAuth = mock(FirebaseAuth.class);
                UserRecord mockRecord = mock(UserRecord.class);
                mockedAuth.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
                when(mockAuth.getUser(MOCK_USER_UID)).thenReturn(mockRecord);

                Map<String, Object> result = focusSessionService.joinSharedFocusRoom("room-999");

                assertNotNull(result);
                assertEquals("Successfully joined shared focus room", result.get("message"));
                assertEquals("room-999", result.get("roomId"));
            }
        }
    }

}
