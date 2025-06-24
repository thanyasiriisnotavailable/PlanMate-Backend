package senior.project.service;

import senior.project.dto.*;

import java.util.List;

public interface StudySetupService {
    TermResponseDTO getTermById(String userUid, Long termId);
    TermResponseDTO getCurrentTerm(String uid);
    TermResponseDTO saveTerm(String userUid, TermRequestDTO termDTO);
    TermResponseDTO updateTerm(String userUid, TermRequestDTO request, Long id);

    List<CourseResponseDTO> saveAllCourses(String userUid, Long termId, List<CourseResponseDTO> courseDTOs);
    void deleteCourse(String userUid, Long termId, String courseCode);

//    void saveAvailabilities(String userUid, List<AvailabilityDTO> availabilityDTOs);
    StudySetupResponseDTO getStudySetup(String userUid);
}