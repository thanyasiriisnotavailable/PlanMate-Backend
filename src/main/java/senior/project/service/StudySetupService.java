package senior.project.service;

import jakarta.transaction.Transactional;
import senior.project.dto.*;
import senior.project.dto.plan.StudySetupDTO;

import java.util.List;

public interface StudySetupService {
    TermResponseDTO getTermById(Long termId);
    TermResponseDTO getCurrentTerm();

    @Transactional
    TermResponseDTO saveTerm(TermRequestDTO termDTO, Long termId);

    List<CourseResponseDTO> saveAllCourses(Long termId, List<CourseResponseDTO> courseDTOs);
    void deleteCourse(Long courseId);

    List<AvailabilityDTO> updateAvailabilities(List<AvailabilityRequestDTO> availabilities);

    @Transactional
    List<AvailabilityDTO> getAvailabilities();

    StudySetupDTO getStudySetup();

    CourseResponseDTO updateCourseDetails(CourseResponseDTO details);

    void processStudySetup(StudySetupDTO dto);
}