package senior.project.service;

import jakarta.transaction.Transactional;
import senior.project.dto.*;
import senior.project.entity.Term;

import java.util.List;
import java.util.Optional;

public interface StudySetupService {
    TermResponseDTO getCurrentTerm(String uid);

    TermResponseDTO saveTerm(String userUid, TermRequestDTO termDTO);

    @Transactional
    TermResponseDTO updateTerm(String userUid, TermRequestDTO request, Long id);

    void saveCourses(String userUid, List<CourseDTO> courseDTOs);
    void saveCourseDetails(String userUid, List<CourseDTO> courseDTOs);
    void saveAvailabilities(String userUid, List<AvailabilityDTO> availabilityDTOs);
    StudySetupResponseDTO getStudySetup(String userUid);
    TermResponseDTO getTermById(String uid, Long termId);
}