package senior.project.service;

import jakarta.transaction.Transactional;
import senior.project.dto.*;
import senior.project.dto.plan.StudySetupDTO;

import java.util.List;

public interface StudySetupService {
    TermResponseDTO getTermById(Long termId);
    TermResponseDTO getCurrentTerm();
    TermResponseDTO saveTerm(TermRequestDTO termDTO);
    TermResponseDTO updateTerm(TermRequestDTO request, Long id);

    List<CourseResponseDTO> saveAllCourses(Long termId, List<CourseResponseDTO> courseDTOs);
    void deleteCourse(Long termId, String courseCode);

    @Transactional
    CourseResponseDTO getCourseDetails(Long termId, String courseCode);

//    void saveAvailabilities(String userUid, List<AvailabilityDTO> availabilityDTOs);
    StudySetupDTO getStudySetup();

    CourseResponseDTO updateCourseDetails(Long termId, String courseCode, CourseResponseDTO details);

    void processStudySetup(StudySetupDTO dto);
}