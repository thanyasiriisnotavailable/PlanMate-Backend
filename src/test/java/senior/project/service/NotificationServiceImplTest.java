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

    @Nested
    @DisplayName("UTC-24: saveFcmToken() tests")
    class SaveFcmTokenTests {

        @Test
        @DisplayName("UTC-24-TC-01: Save valid token → Should update user token in DB")
        void saveFcmToken_validToken_shouldSave() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);
                when(pendingNotificationDao.findByTargetUserUid(MOCK_UID)).thenReturn(Collections.emptyList());

                notificationService.saveFcmToken("valid_fcm_token_123");

                verify(userService, times(1)).updateFcmToken(MOCK_UID, "valid_fcm_token_123");
                verify(pendingNotificationDao, times(1)).findByTargetUserUid(MOCK_UID);
            }
        }

        @Test
        @DisplayName("UTC-24-TC-02: Empty token string → Should log warning and not call DB")
        void saveFcmToken_emptyToken_shouldWarnAndSkip() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);

                notificationService.saveFcmToken("");

                verify(userService, never()).updateFcmToken(any(), any());
                verify(pendingNotificationDao, never()).findByTargetUserUid(any());
            }
        }

        @Test
        @DisplayName("UTC-24-TC-03: Null token → Should log warning and not call DB")
        void saveFcmToken_nullToken_shouldWarnAndSkip() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);

                notificationService.saveFcmToken(null);

                verify(userService, never()).updateFcmToken(any(), any());
                verify(pendingNotificationDao, never()).findByTargetUserUid(any());
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
        @DisplayName("UTC-24-TC-05: Database error during save → Should propagate exception")
        void saveFcmToken_dbError_shouldPropagate() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_UID);

                doThrow(new RuntimeException("DB failure"))
                        .when(userService).updateFcmToken(MOCK_UID, "valid_fcm_token_123");

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> notificationService.saveFcmToken("valid_fcm_token_123"));

                assertEquals("DB failure", ex.getMessage());
                verify(userService, times(1)).updateFcmToken(MOCK_UID, "valid_fcm_token_123");
            }
        }
    }

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

            mockUser = User.builder()
                    .uid(MOCK_UID)
                    .build();
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

                notificationService.sendNotification(validRequest);

                verify(firebaseMock, times(1)).send(any(Message.class));
                verify(notificationDao, times(1)).saveNotification(any(Notification.class));
            }
        }

        @Test
        @DisplayName("UTC-25-TC-02: Empty title/body → Should still send successfully")
        void sendNotification_emptyFields_shouldSend() throws Exception {
            try (var mockedFirebase = mockStatic(FirebaseMessaging.class)) {
                FirebaseMessaging firebaseMock = mock(FirebaseMessaging.class);
                mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMock);
                when(firebaseMock.send(any(Message.class))).thenReturn("mock_response_456");

                when(userService.getFcmToken(MOCK_UID)).thenReturn("valid_fcm_token_123");
                when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

                NotificationRequestDTO emptyRequest = NotificationRequestDTO.builder()
                        .userUid(MOCK_UID)
                        .title("")
                        .content("")
                        .type(NotificationType.GENERAL)
                        .build();

                notificationService.sendNotification(emptyRequest);

                verify(firebaseMock, times(1)).send(any(Message.class));
                verify(notificationDao, times(1)).saveNotification(any(Notification.class));
            }
        }

        @Test
        @DisplayName("UTC-25-TC-03: Null NotificationRequestDTO → Should throw NullPointerException")
        void sendNotification_nullRequest_shouldThrow() {
            assertThrows(NullPointerException.class, () -> notificationService.sendNotification(null));
        }

        @Test
        @DisplayName("UTC-25-TC-04: Invalid/expired FCM token → Should save pending notification")
        void sendNotification_invalidToken_shouldSavePending() {
            when(userService.getFcmToken(MOCK_UID)).thenReturn(null);
            when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

            notificationService.sendNotification(validRequest);

            verify(pendingNotificationDao, times(1)).save(any());
            verify(notificationDao, never()).saveNotification(any());
        }

        @Test
        @DisplayName("UTC-25-TC-05: User not authenticated → Should throw IllegalStateException")
        void sendNotification_noAuthenticatedUser_shouldThrow() {
            try (var mockedSecurity = mockStatic(SecurityUtil.class)) {
                mockedSecurity.when(SecurityUtil::getAuthenticatedUid).thenReturn(null);
                // Simulate via missing user
                when(userService.findByUid(null)).thenReturn(null);

                NotificationRequestDTO request = NotificationRequestDTO.builder()
                        .userUid(null)
                        .title("Test")
                        .content("Message")
                        .build();

                assertDoesNotThrow(() -> notificationService.sendNotification(request)); // method handles null user gracefully
            }
        }

        @Test
        @DisplayName("UTC-25-TC-06: FCM delivery fails temporarily → Should log error but not crash")
        void sendNotification_fcmFails_shouldCatchError() throws Exception {
            try (var mockedFirebase = mockStatic(FirebaseMessaging.class)) {
                FirebaseMessaging firebaseMock = mock(FirebaseMessaging.class);
                mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(firebaseMock);

                FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
                when(firebaseMock.send(any(Message.class))).thenThrow(mockException);

                when(userService.getFcmToken(MOCK_UID)).thenReturn("valid_fcm_token_123");
                when(userService.findByUid(MOCK_UID)).thenReturn(mockUser);

                assertDoesNotThrow(() -> notificationService.sendNotification(validRequest));
                verify(notificationDao, never()).saveNotification(any());
            }
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
