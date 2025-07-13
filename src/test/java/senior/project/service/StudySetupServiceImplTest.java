package senior.project.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import senior.project.dao.*;
import senior.project.dto.*;
import senior.project.entity.*;
import senior.project.exception.ValidationException;
import senior.project.service.impl.StudySetupServiceImpl;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StudySetupServiceImplTest {

    // Mocks for DAOs and Mappers
    @Mock private UserDao userDao;
    @Mock private TermDao termDao;
    @Mock private CourseDao courseDao;
    @Mock private TopicDao topicDao;
    @Mock private ExamDao examDao;
    @Mock private AssignmentDao assignmentDao;
    @Mock private AvailabilityDao availabilityDao;
    @Mock private DTOMapper mapper;

    @InjectMocks
    private StudySetupServiceImpl studySetupService;

    // Common Test Data
    private static final String MOCK_USER_UID = "test-user-123";
    private User mockUser;
    private Term mockTerm;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setUid(MOCK_USER_UID);

        mockTerm = new Term();
        mockTerm.setTermId(1L);
        mockTerm.setUser(mockUser);
        mockTerm.setName("Original Term Name");
        mockTerm.setStartDate(LocalDate.parse("2025-01-01"));
        mockTerm.setEndDate(LocalDate.parse("2025-05-01"));
    }

    // Helper to mock fetching the current authenticated user
    private void mockAuthenticatedUser() {
        // This static mock is common for most tests
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
            when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
        }
    }

    @Nested
    @DisplayName("Tests for saveTerm(TermRequestDTO dto, Long termId)")
    class SaveOrUpdateTermTests {

        @Test
        @DisplayName("UTC-06-TC-01: Save term with valid name and dates (Create)")
        void saveTerm_withValidDataAndNullId_shouldCreateTerm() {
            // Arrange
            TermRequestDTO requestDTO = new TermRequestDTO();
            requestDTO.setName("2/2025");
            requestDTO.setStartDate(LocalDate.parse("2025-06-01"));
            requestDTO.setEndDate(LocalDate.parse("2025-10-31"));

            Term newTerm = new Term(); // The term to be saved
            Term savedTerm = new Term(); // The term after it's saved (with ID)
            savedTerm.setTermId(1L);
            savedTerm.setName(requestDTO.getName());

            TermResponseDTO responseDTO = new TermResponseDTO();
            responseDTO.setTermId(1L);
            responseDTO.setName(requestDTO.getName());

            try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                // Mocks for creation path
                when(termDao.save(any(Term.class))).thenReturn(savedTerm);
                when(mapper.toTermDto(savedTerm)).thenReturn(responseDTO);

                // Act
                // Pass null for termId to signify creation
                TermResponseDTO result = studySetupService.saveTerm(requestDTO, null);

                // Assert
                assertNotNull(result);
                assertEquals(requestDTO.getName(), result.getName());
                verify(termDao).save(any(Term.class));
                verify(termDao, never()).findById(anyLong()); // Should not look for existing term
            }
        }

        @Test
        @DisplayName("UTC-06-TC-02: Update an existing term with valid name and dates")
        void saveTerm_withValidDataAndExistingId_shouldUpdateTerm() {
            // Arrange
            final Long existingTermId = 1L;
            TermRequestDTO requestDTO = new TermRequestDTO();
            requestDTO.setName("Fall Semester 2025");
            requestDTO.setStartDate(LocalDate.parse("2025-08-15"));
            requestDTO.setEndDate(LocalDate.parse("2025-12-20"));

            TermResponseDTO responseDTO = new TermResponseDTO();
            responseDTO.setTermId(existingTermId);
            responseDTO.setName(requestDTO.getName());

            try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);

                // Mocks for update path
                when(termDao.findById(existingTermId)).thenReturn(Optional.of(mockTerm));
                when(termDao.save(any(Term.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved term
                when(mapper.toTermDto(any(Term.class))).thenReturn(responseDTO);

                // Act
                TermResponseDTO result = studySetupService.saveTerm(requestDTO, existingTermId);

                // Assert
                assertNotNull(result);
                assertEquals(requestDTO.getName(), result.getName());

                ArgumentCaptor<Term> termCaptor = ArgumentCaptor.forClass(Term.class);
                verify(termDao).save(termCaptor.capture());
                assertEquals("Fall Semester 2025", termCaptor.getValue().getName()); // Verify the name was updated before saving
            }
        }

        @Test
        @DisplayName("UTC-06-TC-03: Fail when startDate is after endDate")
        void saveTerm_whenStartDateIsAfterEndDate_shouldThrowValidationException() {
            TermRequestDTO requestDTO = new TermRequestDTO();
            requestDTO.setStartDate(LocalDate.parse("2025-10-31"));
            requestDTO.setEndDate(LocalDate.parse("2025-06-01"));
            requestDTO.setName("Invalid Term");

            // Test for both create and update scenarios
            assertThrows(ValidationException.class, () -> studySetupService.saveTerm(requestDTO, null));
            assertThrows(ValidationException.class, () -> studySetupService.saveTerm(requestDTO, 1L));
        }

        @Test
        @DisplayName("UTC-06-TC-04: Fail when startDate equals endDate")
        void saveTerm_whenStartDateEqualsEndDate_shouldThrowValidationException() {
            TermRequestDTO requestDTO = new TermRequestDTO();
            requestDTO.setStartDate(LocalDate.parse("2025-06-01"));
            requestDTO.setEndDate(LocalDate.parse("2025-06-01"));
            requestDTO.setName("Invalid Term");

            assertThrows(ValidationException.class, () -> studySetupService.saveTerm(requestDTO, null));
        }

        @Test
        @DisplayName("UTC-06-TC-05: Fail when name is empty")
        void saveTerm_whenNameIsEmpty_shouldThrowValidationException() {
            TermRequestDTO requestDTO = new TermRequestDTO();
            requestDTO.setName("");
            requestDTO.setStartDate(LocalDate.parse("2025-06-01"));
            requestDTO.setEndDate(LocalDate.parse("2025-10-31"));

            assertThrows(ValidationException.class, () -> studySetupService.saveTerm(requestDTO, null));
        }

        @Test
        @DisplayName("Fail to update when termId does not exist")
        void saveTerm_withNonExistentId_shouldThrowNoSuchElementException() {
            // Arrange
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(termDao.findById(99L)).thenReturn(Optional.empty());

                TermRequestDTO requestDTO = new TermRequestDTO("Update", LocalDate.now(), LocalDate.now().plusDays(1));

                // Act & Assert
                assertThrows(NoSuchElementException.class, () -> studySetupService.saveTerm(requestDTO, 99L));
            }
        }

        @Test
        @DisplayName("Fail to update when user is not authorized")
        void saveTerm_whenUserIsNotOwner_shouldThrowSecurityException() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setUid("another-user-456");
            mockTerm.setUser(anotherUser); // Term belongs to someone else

            try (MockedStatic<SecurityUtil> mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(userDao.findByUid(MOCK_USER_UID)).thenReturn(mockUser);
                when(termDao.findById(1L)).thenReturn(Optional.of(mockTerm));

                TermRequestDTO requestDTO = new TermRequestDTO("Update", LocalDate.now(), LocalDate.now().plusDays(1));

                // Act & Assert
                assertThrows(SecurityException.class, () -> studySetupService.saveTerm(requestDTO, 1L));
            }
        }
    }

    @Nested
    @DisplayName("Tests for saveAllCourses(Long termId, List<CourseResponseDTO> dtos)")
    class SaveAllCoursesTests {

        @BeforeEach
        void setup() {
            // Common setup for these tests
            when(termDao.findById(anyLong())).thenReturn(Optional.of(mockTerm));
            when(courseDao.findByTerm(mockTerm)).thenReturn(new ArrayList<>());
        }

        private CourseResponseDTO createCourseDTO(Long id, String code, String name, Integer credit) {
            CourseResponseDTO dto = new CourseResponseDTO();
            dto.setCourseId(id);
            dto.setCourseCode(code);
            dto.setName(name);
            dto.setCredit(Long.valueOf(credit));
            return dto;
        }

        @Test
        @DisplayName("UTC-07-TC-01: Save multiple valid courses")
        void saveAllCourses_withValidCourses_shouldSucceed() {
            try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(termDao.findById(1L)).thenReturn(Optional.of(mockTerm));

                List<CourseResponseDTO> dtoList = Arrays.asList(
                        createCourseDTO(null, "CS101", "Intro to CS", 3),
                        createCourseDTO(null, "MA101", "Calculus I", 4)
                );

                // Assume no courses exist in the DB for this term yet
                when(courseDao.findByTerm(mockTerm)).thenReturn(new ArrayList<>());

                when(courseDao.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

                // Mock the mapper methods used in the service
                doNothing().when(mapper).updateCourseFromDto(any(), any());
                when(mapper.toCourseResponseDtoList(any())).thenReturn(new ArrayList<>());

                List<CourseResponseDTO> result = studySetupService.saveAllCourses(1L, dtoList);

                ArgumentCaptor<List<Course>> captor = ArgumentCaptor.forClass(List.class);
                verify(courseDao).saveAll(captor.capture());
                verify(courseDao, never()).save(any(Course.class));
                assertEquals(2, captor.getValue().size());
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("UTC-07-TC-02: Fail when course name is empty")
        void saveAllCourses_withEmptyNameOrCode_shouldThrowException() {
            try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                List<CourseResponseDTO> dtoListName = List.of(createCourseDTO(null, "CS101", "", 3));

                ValidationException exName = assertThrows(ValidationException.class, () -> studySetupService.saveAllCourses(1L, dtoListName));
                assertThat(exName.getMessage()).isEqualTo("Course name cannot be empty.");
            }
        }

        @Test
        @DisplayName("UTC-07-TC-03: Fail when course credit is invalid")
        void saveAllCourses_withInvalidCredit_shouldThrowException() {
            try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                CourseResponseDTO invalidCourse = createCourseDTO(null, "CS101", "Intro", 0);
                List<CourseResponseDTO> dtoList = List.of(invalidCourse);

                when(courseDao.findByTerm(mockTerm)).thenReturn(new ArrayList<>());

                ValidationException ex = assertThrows(ValidationException.class, () -> {
                    studySetupService.saveAllCourses(1L, dtoList);
                });
                assertThat(ex.getMessage()).isEqualTo("Invalid course credit for: " + invalidCourse.getName());
            }
        }

        @Test
        @DisplayName("UTC-07-TC-04: Fail with duplicate course codes")
        void saveAllCourses_withDuplicateCourseCode_shouldThrowException() {
            try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(courseDao.findByTerm(mockTerm)).thenReturn(new ArrayList<>());

                CourseResponseDTO course1 = createCourseDTO(null, "CS101", "Intro to CS", 3);
                CourseResponseDTO course2 = createCourseDTO(null, "CS101", "Advanced CS", 3);
                List<CourseResponseDTO> dtoList = Arrays.asList(course1, course2);

                ValidationException ex = assertThrows(ValidationException.class, () -> {
                    studySetupService.saveAllCourses(1L, dtoList);
                });
                assertThat(ex.getMessage()).isEqualTo("Duplicate course code found in list: " + course1.getCourseCode());
            }
        }

        @Test
        @DisplayName("UTC-07-TC-05: Fail with duplicate course names")
        void saveAllCourses_withDuplicateCourseName_shouldThrowException() {
            try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);
                when(courseDao.findByTerm(mockTerm)).thenReturn(new ArrayList<>());

                CourseResponseDTO course1 = createCourseDTO(null, "CS101", "Computer Science", 3);
                CourseResponseDTO course2 = createCourseDTO(null, "CS102", "Computer Science", 3);
                List<CourseResponseDTO> dtoList = Arrays.asList(course1, course2);

                ValidationException ex = assertThrows(ValidationException.class, () -> {
                    studySetupService.saveAllCourses(1L, dtoList);
                });
                assertThat(ex.getMessage()).isEqualTo("Duplicate course name found in list: " + course1.getName());
            }
        }

        @Test
        @DisplayName("UTC-07-TC-06: Fail when course code is empty")
        void saveAllCourses_withEmptyCode_shouldThrowException() {
            try (MockedStatic<SecurityUtil> mocked = Mockito.mockStatic(SecurityUtil.class)) {
                mocked.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

                List<CourseResponseDTO> dtoListCode = List.of(createCourseDTO(null, null, "Intro to CS", 3));

                ValidationException exCode = assertThrows(ValidationException.class, () -> studySetupService.saveAllCourses(1L, dtoListCode));
                assertThat(exCode.getMessage()).isEqualTo("Course code cannot be empty.");
            }
        }
    }

    @Nested
    @DisplayName("UTC-08: Tests for saveTopics(List<TopicDTO> dtos, Course course)")
    class SaveTopicsTests {

        private Course mockCourse;
        private User courseOwner;

        @BeforeEach
        void initTopicTest() {
            courseOwner = new User();
            courseOwner.setUid(MOCK_USER_UID);

            mockCourse = new Course();
            mockCourse.setCourseId(10L);
            mockCourse.setName("Test Course");
            mockCourse.setTerm(mockTerm);
            mockTerm.setUser(courseOwner);
        }

        private TopicDTO createTopicDTO(String id, String name) {
            TopicDTO dto = new TopicDTO();
            dto.setId(id);
            dto.setName(name);
            return dto;
        }

        private Topic createTopicEntity(String id, String name, Course course) {
            Topic topic = new Topic();
            topic.setId(id);
            topic.setName(name);
            topic.setCourse(course);
            return topic;
        }

        @Test
        @DisplayName("UTC-08-TC-01: Create new topic when none exist")
        void saveTopics_withNewTopic_shouldCreate() {
            TopicDTO newDto = createTopicDTO("t2", "Physics");

            when(topicDao.findByCourse(mockCourse)).thenReturn(Collections.emptyList());

            Topic mappedTopic = createTopicEntity("t2", "Physics", mockCourse);
            when(mapper.toTopic(newDto)).thenReturn(mappedTopic);
            when(topicDao.save(any(Topic.class))).thenReturn(mappedTopic);

            Map<String, Topic> result = studySetupService.saveTopics(List.of(newDto), mockCourse);

            assertEquals(1, result.size());
            assertEquals("Physics", result.get("t2").getName());
            verify(topicDao).save(any(Topic.class));
        }

        @Test
        @DisplayName("UTC-08-TC-02: Update existing topic")
        void saveTopics_withExistingTopic_shouldUpdate() {
            Topic existingTopic = createTopicEntity("t1", "Math", mockCourse);
            TopicDTO updateDto = createTopicDTO("t1", "Advanced Math");

            when(topicDao.findByCourse(mockCourse)).thenReturn(List.of(existingTopic));
            doAnswer(invocation -> {
                TopicDTO dto = invocation.getArgument(0);
                Topic topic = invocation.getArgument(1);
                topic.setName(dto.getName());
                return null;
            }).when(mapper).updateTopicFromDto(any(), any());

            when(topicDao.save(any(Topic.class))).thenReturn(existingTopic);

            Map<String, Topic> result = studySetupService.saveTopics(List.of(updateDto), mockCourse);

            assertEquals(1, result.size());
            assertEquals("Advanced Math", result.get("t1").getName());
        }

        @Test
        @DisplayName("UTC-08-TC-03: Delete all topics when DTO list is null")
        void saveTopics_withNullList_shouldDeleteAll() {
            when(topicDao.findByCourse(mockCourse)).thenReturn(List.of(createTopicEntity("t3", "Old Topic", mockCourse)));

            Map<String, Topic> result = studySetupService.saveTopics(null, mockCourse);

            assertTrue(result.isEmpty());
            verify(topicDao).deleteByCourse(mockCourse);
        }

        @Test
        @DisplayName("UTC-08-TC-04: Fail when trying to update topic not owned by user")
        void saveTopics_withUnauthorizedTopic_shouldThrowSecurityException() {
            // Set up a topic belonging to another user
            User anotherUser = new User();
            anotherUser.setUid("other-user");

            Course otherCourse = new Course();
            otherCourse.setCourseId(11L);
            otherCourse.setTerm(mockTerm);
            mockTerm.setUser(anotherUser); // owner is different

            Topic existingTopic = createTopicEntity("t4", "Chem", otherCourse);
            TopicDTO updateDto = createTopicDTO("t4", "New Chem");

            when(topicDao.findByCourse(otherCourse)).thenReturn(List.of(existingTopic));

            assertThrows(SecurityException.class, () -> studySetupService.saveTopics(List.of(updateDto), otherCourse));
        }

        @Test
        @DisplayName("UTC-08-TC-05: Fail when topic ID is missing (null)")
        void saveTopics_withMissingId_shouldThrowValidation() {
            TopicDTO invalidDto = createTopicDTO(null, "Biology");

            assertThrows(ValidationException.class, () -> studySetupService.saveTopics(List.of(invalidDto), mockCourse));
        }
    }
}