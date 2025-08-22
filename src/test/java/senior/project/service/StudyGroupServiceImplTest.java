package senior.project.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import senior.project.dao.*;
import senior.project.dto.GroupMemberProgressDTO;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.entity.*;
import senior.project.service.impl.StudyGroupServiceImpl;
import senior.project.util.SecurityUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyGroupServiceImplTest {

    @Mock
    private StudyGroupDao studyGroupDao;
    @Mock
    private GroupMemberDao groupMemberDao;
    @Mock
    private SessionDao sessionDao;
    @Mock
    private FocusSessionDao focusSessionDao;
    @Mock
    private FirebaseAuth firebaseAuth;
    @Mock
    private UserDao userDao;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    private static final String MOCK_USER_UID = "user123";
    private static final String VALID_JOIN_CODE = "ABC123";

    private User mockUser;
    private StudyGroup mockGroup;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setUid(MOCK_USER_UID);

        mockGroup = StudyGroup.builder()
                .name("Math Final")
                .joinCode(VALID_JOIN_CODE)
                .build();
    }

    // ========== UC-13: Create Group ==========
    @Nested
    @DisplayName("Test for createGroup(GroupRequestDTO groupInfo)")
    class CreateGroupTests {
        @Test
        @DisplayName("UTC-13-TC-01: Create group with valid input")
        void createGroup_validInput_shouldSucceed() {
            GroupRequestDTO dto = new GroupRequestDTO("Math Final", "math.png");

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(studyGroupDao.existsByJoinCode(anyString())).thenReturn(false);

                ResponseEntity<?> response = studyGroupService.createGroup(dto);

                assertEquals(200, response.getStatusCodeValue());
                assertTrue(((String) response.getBody()).matches("[A-Z0-9]{6}"));
            }
        }

        @Test
        @DisplayName("UTC-13-TC-02: Create group with empty name should fail")
        void createGroup_emptyName_shouldFail() {
            GroupRequestDTO dto = new GroupRequestDTO("", "img.png");
            ResponseEntity<?> response = studyGroupService.createGroup(dto);

            assertEquals(400, response.getStatusCodeValue());
            assertTrue(response.getBody().toString().contains("Group name is required"));
        }

        @Test
        @DisplayName("UTC-13-TC-03: Create group with long name should fail")
        void createGroup_longName_shouldFail() {
            GroupRequestDTO dto = new GroupRequestDTO("A".repeat(51), "img.png");
            ResponseEntity<?> response = studyGroupService.createGroup(dto);

            assertEquals(400, response.getStatusCodeValue());
            assertTrue(response.getBody().toString().contains("Group name must be less than 50 characters."));
        }

        @Test
        @DisplayName("UTC-13-TC-04: Create group with null image should succeed")
        void createGroup_nullImage_shouldSucceed() {
            GroupRequestDTO dto = new GroupRequestDTO("Final Review", null);

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(studyGroupDao.existsByJoinCode(anyString())).thenReturn(false);

                ResponseEntity<?> response = studyGroupService.createGroup(dto);

                assertEquals(200, response.getStatusCodeValue());
            }
        }

        @Test
        @DisplayName("UTC-13-TC-05: Database error when saving group should return 500")
        void createGroup_dbError_shouldReturn500() {
            GroupRequestDTO dto = new GroupRequestDTO("Math Final", "math.png");

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(studyGroupDao.existsByJoinCode(anyString())).thenReturn(false);
                doThrow(new RuntimeException("DB error")).when(studyGroupDao).save(any());

                ResponseEntity<?> response = studyGroupService.createGroup(dto);

                assertEquals(500, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("Group creation failed"));
            }
        }

        @Test
        @DisplayName("UTC-13-TC-06: Ensure join code is 6 characters")
        void createGroup_joinCodeLengthCheck() {
            GroupRequestDTO dto = new GroupRequestDTO("Test", null);

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                // Arrange
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(studyGroupDao.existsByJoinCode(anyString())).thenReturn(false);

                // Act
                ResponseEntity<?> response = studyGroupService.createGroup(dto);
                String code = response.getBody().toString();

                // Debug (optional)
                System.out.println("Join code: " + code);

                // Assert
                assertEquals(200, response.getStatusCodeValue());
                assertEquals(6, code.length());
                assertTrue(code.matches("[A-Z0-9]{6}"));
            }
        }

        @Test
        @DisplayName("UTC-13-TC-07: Code generated is unique")
        void createGroup_generatedCodeShouldBeUnique() {
            GroupRequestDTO dto = new GroupRequestDTO("Math Final", "math.png");

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                Set<String> joinCodes = new HashSet<>();

                // Generate 10 groups and collect the join codes
                for (int i = 0; i < 10; i++) {
                    when(studyGroupDao.existsByJoinCode(anyString())).thenReturn(false);
                    ResponseEntity<?> response = studyGroupService.createGroup(dto);
                    assertEquals(200, response.getStatusCodeValue());

                    String code = response.getBody().toString();
                    assertTrue(code.matches("[A-Z0-9]{6}"));

                    boolean isUnique = joinCodes.add(code);  // false if duplicate
                    assertTrue(isUnique, "Duplicate join code generated: " + code);
                }

                // Optional debug print
                System.out.println("Generated join codes: " + joinCodes);
            }
        }
    }


    // ========== UC-14: Join Group ==========
    @Nested
    @DisplayName("Tests for joinGroup(JoinGroupRequestDTO dto)")
    class JoinGroupTests {
        @Test
        @DisplayName("UTC-14-TC-01: Join with valid code should succeed")
        void joinGroup_validCode_shouldSucceed() {

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(studyGroupDao.findByJoinCode(VALID_JOIN_CODE)).thenReturn(Optional.of(mockGroup));
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.existsByUserAndGroup(mockUser, mockGroup)).thenReturn(false);

                    ResponseEntity<?> response = studyGroupService.joinGroup(VALID_JOIN_CODE);

                assertEquals(200, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("Joined group"));
            }
        }

        @Test
        @DisplayName("UTC-14-TC-02: Join code not found")
        void joinGroup_codeNotFound_shouldFail() {
            when(studyGroupDao.findByJoinCode("ZZZZZZ")).thenReturn(Optional.empty());

            ResponseEntity<?> response = studyGroupService.joinGroup("ZZZZZZ");

            assertEquals(400, response.getStatusCodeValue());
            assertTrue(response.getBody().toString().contains("Invalid join code"));
        }

        @Test
        @DisplayName("UTC-14-TC-03: User already member")
        void joinGroup_userAlreadyMember_shouldFail() {
            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(studyGroupDao.findByJoinCode(VALID_JOIN_CODE)).thenReturn(Optional.of(mockGroup));
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.existsByUserAndGroup(mockUser, mockGroup)).thenReturn(true);

                ResponseEntity<?> response = studyGroupService.joinGroup(VALID_JOIN_CODE);

                assertEquals(400, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("already a member"));
            }
        }

        @Test
        @DisplayName("UTC-14-TC-04: Exception during join should return 500")
        void joinGroup_exception_shouldReturn500() {
            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(studyGroupDao.findByJoinCode(VALID_JOIN_CODE)).thenThrow(new RuntimeException("DB failure"));

                ResponseEntity<?> response = studyGroupService.joinGroup(VALID_JOIN_CODE);

                assertEquals(500, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("Network issue"));
            }
        }
    }

    // ========== UC-15: Get Group Progress ==========
    @Nested
    @DisplayName("Tests for getGroupProgress(Long groupId)")
    class GetGroupProgressTests {

        private GroupMember member1, member2, member3;

        @BeforeEach
        void setupMembers() {
            User user1 = new User(); user1.setUid("uid1");
            User user2 = new User(); user2.setUid("uid2");
            User user3 = new User(); user3.setUid("uid3");

            member1 = new GroupMember(); member1.setUser(user1);
            member2 = new GroupMember(); member2.setUser(user2);
            member3 = new GroupMember(); member3.setUser(user3);
        }

        @Test
        @DisplayName("UTC-15-TC-01: Group has multiple members with different progress")
        void getProgress_multipleMembers_shouldReturnSortedByPoints() throws Exception {
            when(groupMemberDao.findByGroupId(1L)).thenReturn(List.of(member1, member2));

            // Member1: 10 planned, 8 completed, 7200 focus seconds (2h)
            when(sessionDao.countTotalPlannedSessionsForUser("uid1")).thenReturn(10);
            when(sessionDao.countCompletedSessionsForUser("uid1")).thenReturn(8);
            when(focusSessionDao.sumFocusSecondsByUser("uid1")).thenReturn(7200L);

            // Member2: 5 planned, 5 completed, 3600 focus seconds (1h)
            when(sessionDao.countTotalPlannedSessionsForUser("uid2")).thenReturn(5);
            when(sessionDao.countCompletedSessionsForUser("uid2")).thenReturn(5);
            when(focusSessionDao.sumFocusSecondsByUser("uid2")).thenReturn(3600L);

            // Firebase mock
            UserRecord record1 = mock(UserRecord.class);
            when(record1.getDisplayName()).thenReturn("Alice");
            when(record1.getPhotoUrl()).thenReturn("alice.png");
            UserRecord record2 = mock(UserRecord.class);
            when(record2.getDisplayName()).thenReturn("Bob");
            when(record2.getPhotoUrl()).thenReturn("bob.png");

            when(firebaseAuth.getUser("uid1")).thenReturn(record1);
            when(firebaseAuth.getUser("uid2")).thenReturn(record2);

            List<GroupMemberProgressDTO> result = studyGroupService.getGroupProgress(1L);

            assertEquals(2, result.size());
            // Points: Member1 = (8*10) + (2*5) = 90, Member2 = (5*10) + (1*5) = 55
            assertEquals("uid1", result.get(0).getMember().getMemberId()); // highest points first
            assertEquals(90, result.get(0).getPoints());
            assertEquals(80.0, result.get(0).getPercentageCompleted());
        }

        @Test
        @DisplayName("UTC-15-TC-02: Group has no members")
        void getProgress_emptyGroup_shouldReturnEmptyList() {
            when(groupMemberDao.findByGroupId(2L)).thenReturn(List.of());
            List<GroupMemberProgressDTO> result = studyGroupService.getGroupProgress(2L);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("UTC-15-TC-03: Member has 0 total planned sessions")
        void getProgress_zeroPlannedSessions_shouldHaveZeroPercent() throws Exception {
            when(groupMemberDao.findByGroupId(3L)).thenReturn(List.of(member1));

            when(sessionDao.countTotalPlannedSessionsForUser("uid1")).thenReturn(0);
            when(sessionDao.countCompletedSessionsForUser("uid1")).thenReturn(5); // completed > planned
            when(focusSessionDao.sumFocusSecondsByUser("uid1")).thenReturn(3600L);

            UserRecord record = mock(UserRecord.class);
            when(record.getDisplayName()).thenReturn("Alice");
            when(firebaseAuth.getUser("uid1")).thenReturn(record);

            List<GroupMemberProgressDTO> result = studyGroupService.getGroupProgress(3L);
            assertEquals(0.0, result.get(0).getPercentageCompleted());
            assertEquals(55, result.get(0).getPoints()); // (5 tasks * 10) + (1h * 5) = 55? Wait—check formula
        }

        @Test
        @DisplayName("UTC-15-TC-04: Firebase user info fetch throws exception")
        void getProgress_firebaseError_shouldUseUnknownName() throws Exception {
            when(groupMemberDao.findByGroupId(4L)).thenReturn(List.of(member1));

            when(sessionDao.countTotalPlannedSessionsForUser("uid1")).thenReturn(5);
            when(sessionDao.countCompletedSessionsForUser("uid1")).thenReturn(5);
            when(focusSessionDao.sumFocusSecondsByUser("uid1")).thenReturn(0L);

            when(firebaseAuth.getUser("uid1")).thenThrow(new RuntimeException("Firebase down"));

            List<GroupMemberProgressDTO> result = studyGroupService.getGroupProgress(4L);
            assertEquals("Unknown", result.get(0).getMember().getDisplayName());
            assertNull(result.get(0).getMember().getPhotoUrl());
            assertEquals(50, result.get(0).getPoints()); // (5 tasks * 10) + (0h * 5)
        }
    }
}