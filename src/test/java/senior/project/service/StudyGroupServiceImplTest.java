package senior.project.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import senior.project.dao.*;
import senior.project.dto.GroupRequestDTO;
import senior.project.dto.JoinGroupRequestDTO;
import senior.project.entity.*;
import senior.project.service.impl.StudyGroupServiceImpl;
import senior.project.util.SecurityUtil;

import java.util.HashSet;
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
            assertTrue(response.getBody().toString().contains("under 50 characters"));
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

        @Test
        @DisplayName("UTC-13-TC-08: Create group with special characters in name")
        void createGroup_specialChars_shouldSucceed() {
            GroupRequestDTO dto = new GroupRequestDTO("Eng Final @ 2025!", "final.png");

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(studyGroupDao.existsByJoinCode(anyString())).thenReturn(false);

                ResponseEntity<?> response = studyGroupService.createGroup(dto);

                assertEquals(200, response.getStatusCodeValue());
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
            JoinGroupRequestDTO dto = new JoinGroupRequestDTO(VALID_JOIN_CODE);

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(studyGroupDao.findByJoinCode(VALID_JOIN_CODE)).thenReturn(Optional.of(mockGroup));
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.existsByUserAndGroup(mockUser, mockGroup)).thenReturn(false);

                ResponseEntity<?> response = studyGroupService.joinGroup(dto);

                assertEquals(200, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("Joined group"));
            }
        }

        @Test
        @DisplayName("UTC-14-TC-02: Join code not found")
        void joinGroup_codeNotFound_shouldFail() {
            JoinGroupRequestDTO dto = new JoinGroupRequestDTO("ZZZZZZ");
            when(studyGroupDao.findByJoinCode("ZZZZZZ")).thenReturn(Optional.empty());

            ResponseEntity<?> response = studyGroupService.joinGroup(dto);

            assertEquals(400, response.getStatusCodeValue());
            assertTrue(response.getBody().toString().contains("Invalid join code"));
        }

        @Test
        @DisplayName("UTC-14-TC-03: User already member")
        void joinGroup_userAlreadyMember_shouldFail() {
            JoinGroupRequestDTO dto = new JoinGroupRequestDTO(VALID_JOIN_CODE);

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(studyGroupDao.findByJoinCode(VALID_JOIN_CODE)).thenReturn(Optional.of(mockGroup));
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(groupMemberDao.existsByUserAndGroup(mockUser, mockGroup)).thenReturn(true);

                ResponseEntity<?> response = studyGroupService.joinGroup(dto);

                assertEquals(400, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("already a member"));
            }
        }

        @Test
        @DisplayName("UTC-14-TC-04: Exception during join should return 500")
        void joinGroup_exception_shouldReturn500() {
            JoinGroupRequestDTO dto = new JoinGroupRequestDTO(VALID_JOIN_CODE);

            try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
                mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(studyGroupDao.findByJoinCode(VALID_JOIN_CODE)).thenThrow(new RuntimeException("DB failure"));

                ResponseEntity<?> response = studyGroupService.joinGroup(dto);

                assertEquals(500, response.getStatusCodeValue());
                assertTrue(response.getBody().toString().contains("Network issue"));
            }
        }
    }
}