package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import senior.project.dao.StudyPreferenceDao;
import senior.project.dto.StudyPreferenceDTO;
import senior.project.entity.StudyPreference;
import senior.project.entity.User;
import senior.project.service.StudyPreferenceService;
import senior.project.service.UserService;
import senior.project.util.DTOMapper;
import senior.project.util.SecurityUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyPreferenceServiceImpl implements StudyPreferenceService {

    private final StudyPreferenceDao studyPreferenceDao;
    private final UserService userService;
    private final DTOMapper mapper;
    @Override
    public StudyPreferenceDTO getPreference() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        StudyPreference preference = studyPreferenceDao.findByUserUid(userUid);

        return Optional.ofNullable(preference)
                .map(mapper::toStudyPreferenceDto)
                .orElse(null);
    }

    @Override
    public StudyPreferenceDTO saveOrUpdate(StudyPreferenceDTO dto) {
        if (dto.getMinSessionDuration() <= 0) {
            throw new IllegalArgumentException("Minimum session duration must be greater than 0");
        }
        if (dto.getMaxSessionDuration() < dto.getMinSessionDuration()) {
            throw new IllegalArgumentException("Maximum session duration must be greater than or equal to minimum");
        }
        if (dto.getRevisionFrequency() == null || dto.getRevisionFrequency().trim().isEmpty()) {
            throw new IllegalArgumentException("Revision frequency must not be empty");
        }
        if (dto.getBreakDurations() < 0) {
            throw new IllegalArgumentException("Break duration must be non-negative");
        }

        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userService.findByUid(userUid);
        StudyPreference existingPreference = studyPreferenceDao.findByUserUid(userUid);

        StudyPreference preferenceToSave;
        if (existingPreference != null) {
            preferenceToSave = updateFields(existingPreference, dto);
        } else {
            preferenceToSave = mapper.toStudyPreference(dto, user);
        }

        StudyPreference saved = studyPreferenceDao.savePreference(preferenceToSave);
        return mapper.toStudyPreferenceDto(saved);
    }

    private StudyPreference updateFields(StudyPreference pref, StudyPreferenceDTO dto) {
        pref.setMinSessionDuration(dto.getMinSessionDuration());
        pref.setMaxSessionDuration(dto.getMaxSessionDuration());
        pref.setPreferredStudyTimes(dto.getPreferredStudyTimes());
        pref.setRevisionFrequency(dto.getRevisionFrequency());
        pref.setBreakDurations(dto.getBreakDurations());
        return pref;
    }
}
