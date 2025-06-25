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
    public Optional<StudyPreferenceDTO> getPreference() {
        String userUid = SecurityUtil.getAuthenticatedUid();
        return studyPreferenceDao.findByUserUid(userUid)
                .map(mapper::toStudyPreferenceDto);
    }

    @Override
    public StudyPreferenceDTO saveOrUpdate(StudyPreferenceDTO dto) {
        String userUid = SecurityUtil.getAuthenticatedUid();
        User user = userService.findByUid(userUid);

        StudyPreference preference = studyPreferenceDao.findByUserUid(userUid)
                .map(existing -> updateFields(existing, dto))
                .orElseGet(() -> mapper.toStudyPreference(dto, user));

        StudyPreference saved = studyPreferenceDao.savePreference(preference);
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
