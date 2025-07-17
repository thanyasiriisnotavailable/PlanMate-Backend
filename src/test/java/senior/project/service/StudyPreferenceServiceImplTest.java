package senior.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import senior.project.dao.StudyPreferenceDao;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.User;
import senior.project.exception.ValidationException;
import senior.project.service.impl.StudyPreferenceServiceImpl;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StudyPreferenceServiceImplTest {

    @Mock
    private StudyPreferenceDao studyPreferenceDao;

    @Mock
    private UserService userService;

    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private StudyPreferenceServiceImpl studyPreferenceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Mocked user UID for authenticated session
    private static final String MOCK_USER_UID = "user123";

    private StudyPreferenceDTO getValidDto() {
        StudyPreferenceDTO dto = new StudyPreferenceDTO();
        dto.setMinSessionDuration(30);
        dto.setMaxSessionDuration(90);
        dto.setPreferredStudyTimes(Arrays.asList("Afternoon", "Evening"));
        dto.setRevisionFrequency("2-3");
        dto.setBreakDurations(15);
        return dto;
    }

    private StudyPreference getEntityFromDto(StudyPreferenceDTO dto) {
        StudyPreference pref = new StudyPreference();
        pref.setMinSessionDuration(dto.getMinSessionDuration());
        pref.setMaxSessionDuration(dto.getMaxSessionDuration());
        pref.setPreferredStudyTimes(dto.getPreferredStudyTimes());
        pref.setRevisionFrequency(dto.getRevisionFrequency());
        pref.setBreakDurations(dto.getBreakDurations());
        return pref;
    }

    @Test
    void UTC_05_TC_01_SaveNewPreferenceWithValidInput() {
        StudyPreferenceDTO dto = getValidDto();
        StudyPreference entity = getEntityFromDto(dto);

        User user = new User();
        user.setUid(MOCK_USER_UID);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            when(studyPreferenceDao.findByUserUid(MOCK_USER_UID)).thenReturn(null);
            when(userService.findByUid(MOCK_USER_UID)).thenReturn(user);
            when(dtoMapper.toStudyPreference(dto, user)).thenReturn(entity);
            when(studyPreferenceDao.savePreference(entity)).thenReturn(entity);
            when(dtoMapper.toStudyPreferenceDto(entity)).thenReturn(dto);

            StudyPreferenceDTO result = studyPreferenceService.saveOrUpdate(dto);
            assertThat(result).isEqualTo(dto);
        }
    }

    @Test
    void UTC_05_TC_02_UpdateExistingPreference() {
        StudyPreferenceDTO dto = getValidDto();
        StudyPreference existing = getEntityFromDto(dto);

        User user = new User();
        user.setUid(MOCK_USER_UID);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            when(studyPreferenceDao.findByUserUid(MOCK_USER_UID)).thenReturn(existing);
            when(studyPreferenceDao.savePreference(existing)).thenReturn(existing);
            when(dtoMapper.toStudyPreferenceDto(existing)).thenReturn(dto);

            StudyPreferenceDTO result = studyPreferenceService.saveOrUpdate(dto);
            assertThat(result).isEqualTo(dto);
        }
    }

    @Test
    void UTC_05_TC_03_FailWhenMinSessionDurationIsZero() {
        StudyPreferenceDTO dto = getValidDto();
        dto.setMinSessionDuration(0);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            ValidationException ex = assertThrows(ValidationException.class, () -> {
                studyPreferenceService.saveOrUpdate(dto);
            });
            assertThat(ex.getMessage()).isEqualTo("Minimum session duration must be greater than 0");
        }
    }

    @Test
    void UTC_05_TC_04_FailWhenMaxSessionDurationLessThanMin() {
        StudyPreferenceDTO dto = getValidDto();
        dto.setMinSessionDuration(60);
        dto.setMaxSessionDuration(30);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            ValidationException ex = assertThrows(ValidationException.class, () -> {
                studyPreferenceService.saveOrUpdate(dto);
            });
            assertThat(ex.getMessage()).isEqualTo("Maximum session duration must be greater than or equal to minimum");
        }
    }

    @Test
    void UTC_05_TC_05_FailWhenRevisionFrequencyIsEmpty() {
        StudyPreferenceDTO dto = getValidDto();
        dto.setRevisionFrequency("");

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            ValidationException ex = assertThrows(ValidationException.class, () -> {
                studyPreferenceService.saveOrUpdate(dto);
            });
            assertThat(ex.getMessage()).isEqualTo("Study preference information cannot be empty");
        }
    }

    @Test
    void UTC_05_TC_06_FailWhenBreakDurationIsNegative() {
        StudyPreferenceDTO dto = getValidDto();
        dto.setBreakDurations(-5);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            ValidationException ex = assertThrows(ValidationException.class, () -> {
                studyPreferenceService.saveOrUpdate(dto);
            });
            assertThat(ex.getMessage()).isEqualTo("Break duration must be non-negative");
        }
    }

    @Test
    void UTC_05_TC_07_AllowZeroBreakDuration() {
        StudyPreferenceDTO dto = getValidDto();
        dto.setBreakDurations(0);

        StudyPreference entity = getEntityFromDto(dto);
        User user = new User();
        user.setUid(MOCK_USER_UID);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getAuthenticatedUid).thenReturn(MOCK_USER_UID);

            when(studyPreferenceDao.findByUserUid(MOCK_USER_UID)).thenReturn(null);
            when(userService.findByUid(MOCK_USER_UID)).thenReturn(user);
            when(dtoMapper.toStudyPreference(dto, user)).thenReturn(entity);
            when(studyPreferenceDao.savePreference(entity)).thenReturn(entity);
            when(dtoMapper.toStudyPreferenceDto(entity)).thenReturn(dto);

            StudyPreferenceDTO result = studyPreferenceService.saveOrUpdate(dto);
            assertThat(result).isEqualTo(dto);
        }
    }
}