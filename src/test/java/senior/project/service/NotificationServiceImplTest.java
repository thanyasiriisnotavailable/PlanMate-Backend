package senior.project.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senior.project.dao.NotificationDao;
import senior.project.dao.PendingNotificationDao;
import senior.project.dto.NotificationRequestDTO;
import senior.project.entity.Notification;
import senior.project.entity.User;
import senior.project.enums.NotificationType;
import senior.project.service.impl.NotificationServiceImpl;
import senior.project.util.SecurityUtil;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private PendingNotificationDao pendingNotificationDao;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private static final String MOCK_UID = "uid_123";
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImplTest.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------------------------
    // UTC-24: saveFcmToken() tests
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("UTC-24: saveFcmToken() tests")
    class SaveFcmTokenTests {

        @Test
        @DisplayName("UTC-24-TC-01: Save valid token → Should update user token in DB")
        void saveFcmToken_validToken_shouldSave() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);
                when(pendingNotificationDao.findByTargetUserUid(MOCK_UID)).thenReturn(Collections.emptyList());

                assertDoesNotThrow(() -> notificationService.saveFcmToken("valid_fcm_token_123"));

                verify(userService, times(1)).updateFcmToken(MOCK_UID, "valid_fcm_token_123");
                verify(pendingNotificationDao, times(1)).findByTargetUserUid(MOCK_UID);
            }
        }

        @Test
        @DisplayName("UTC-24-TC-02: Empty token string → Should throw IllegalArgumentException")
        void saveFcmToken_emptyToken_shouldThrow() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> notificationService.saveFcmToken(""));

                assertEquals("FCM token cannot be null or empty", ex.getMessage());
                verify(userService, never()).updateFcmToken(any(), any());
            }
        }

        @Test
        @DisplayName("UTC-24-TC-03: Null token → Should throw IllegalArgumentException")
        void saveFcmToken_nullToken_shouldThrow() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> notificationService.saveFcmToken(null));

                assertEquals("FCM token cannot be null or empty", ex.getMessage());
                verify(userService, never()).updateFcmToken(any(), any());
            }
        }

        @Test
        @DisplayName("UTC-24-TC-04: No authenticated user → Should throw IllegalStateException")
        void saveFcmToken_noAuthenticatedUser_shouldThrow() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(null);

                IllegalStateException ex = assertThrows(IllegalStateException.class,
                        () -> notificationService.saveFcmToken("valid_fcm_token_123"));

                assertEquals("No authenticated user", ex.getMessage());
                verify(userService, never()).updateFcmToken(any(), any());
            }
        }

        @Test
        @DisplayName("UTC-24-TC-05: Database error during save → Should throw RuntimeException with message")
        void saveFcmToken_dbError_shouldThrowRuntime() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);
                doThrow(new RuntimeException("DB failure"))
                        .when(userService).updateFcmToken(MOCK_UID, "valid_fcm_token_123");

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> notificationService.saveFcmToken("valid_fcm_token_123"));

                assertTrue(ex.getMessage().contains("Failed to save FCM token"));
                verify(userService, times(1)).updateFcmToken(MOCK_UID, "valid_fcm_token_123");
            }
        }
    }

    // -------------------------------------------------------------------------
    // UTC-25: sendNotification() tests
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("UTC-25: sendNotification() tests")
    class SendNotificationTests {

        private NotificationRequestDTO validRequest;
        private User mockUser;

        @BeforeEach
        void setupRequest() {
            validRequest = NotificationRequestDTO.builder()
                    .userUid(MOCK_UID)
                    .title("Hello")
                    .content("World")
                    .type(NotificationType.GENERAL)
                    .build();

            mockUser = User.builder().uid(MOCK_UID).build();
        }

        @Test
        @DisplayName("UTC-25-TC-01: Send valid notification → Should send successfully and save to DB")
        void sendNotification_valid_shouldSendAndSave() throws Exception {
            try (var mockedFirebase = mockStatic(FirebaseMessaging.class)) {
                FirebaseMessaging firebaseMock = mock(FirebaseMessaging.class);
                mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMock);
                when(firebaseMock.send(any(Message.class))).thenReturn("mock_response_123");

                when(userService.getFcmToken(MOCK_UID)).thenReturn("valid_fcm_token_123");
                when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

                assertDoesNotThrow(() -> notificationService.sendNotification(validRequest));

                verify(firebaseMock, times(1)).send(any(Message.class));
                verify(notificationDao, times(1)).saveNotification(any(Notification.class));
            }
        }

        @Test
        @DisplayName("UTC-25-TC-02: Empty or null title/body → Should throw IllegalArgumentException")
        void sendNotification_emptyFields_shouldThrow() {
            when(userService.getFcmToken(MOCK_UID)).thenReturn("valid_fcm_token_123");
            when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

            NotificationRequestDTO badRequest = NotificationRequestDTO.builder()
                    .userUid(MOCK_UID)
                    .title("")
                    .content("")
                    .type(NotificationType.GENERAL)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> notificationService.sendNotification(badRequest));

            assertEquals("Notification title and body cannot be null or empty", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-25-TC-03: Null NotificationRequestDTO → Should throw IllegalArgumentException")
        void sendNotification_nullRequest_shouldThrow() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> notificationService.sendNotification(null));

            assertEquals("Notification request cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-25-TC-04: Delivery failure (e.g. expired token) → Should save pending and throw RuntimeException")
        void sendNotification_fcmFails_shouldSavePendingAndThrow() throws Exception {
            try (var mockedFirebase = mockStatic(FirebaseMessaging.class)) {
                FirebaseMessaging firebaseMock = mock(FirebaseMessaging.class);
                mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMock);

                FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
                when(firebaseMock.send(any(Message.class))).thenThrow(mockException);

                when(userService.getFcmToken(MOCK_UID)).thenReturn("valid_fcm_token_123");
                when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> notificationService.sendNotification(validRequest));

                assertTrue(ex.getMessage().contains("Failed to send notification, saved to pending"));
                verify(pendingNotificationDao, times(1)).save(any());
            }
        }

        @Test
        @DisplayName("UTC-25-TC-05: User not found → Should throw IllegalStateException")
        void sendNotification_userNotFound_shouldThrow() {
            when(userService.getFcmToken(MOCK_UID)).thenReturn("valid_fcm_token_123");
            when(userService.findByUid(MOCK_UID)).thenReturn(null);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> notificationService.sendNotification(validRequest));

            assertTrue(ex.getMessage().contains("User not found for UID"));
        }

        @Test
        @DisplayName("UTC-25-TC-06: Missing or empty token → Should throw IllegalStateException")
        void sendNotification_missingToken_shouldThrow() {
            when(userService.getFcmToken(MOCK_UID)).thenReturn("");
            when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> notificationService.sendNotification(validRequest));

            assertEquals("User has no valid FCM token", ex.getMessage());
        }

        @Test
        @DisplayName("UTC-25-TC-07: Database error while fetching token → Should propagate exception")
        void sendNotification_dbError_shouldPropagate() {
            when(userService.getFcmToken(MOCK_UID)).thenThrow(new RuntimeException("DB error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> notificationService.sendNotification(validRequest));

            assertEquals("DB error", ex.getMessage());
        }
    }
}
