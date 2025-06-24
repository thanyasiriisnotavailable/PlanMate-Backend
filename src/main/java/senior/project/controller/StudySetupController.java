package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.*;
import senior.project.service.StudySetupService;

import java.util.List;

@RestController
@RequestMapping("/study-setup")
@RequiredArgsConstructor
public class StudySetupController {

    private final StudySetupService studySetupService;

    // GET: Get full study setup
    @GetMapping
    public ResponseEntity<StudySetupResponseDTO> getStudySetup() {
        String uid = getAuthenticatedUid();
        StudySetupResponseDTO dto = studySetupService.getStudySetup(uid);
        return (dto == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    // GET: Get term by ID
    @GetMapping("/terms/{termId}")
    public ResponseEntity<TermResponseDTO> getTermById(@PathVariable Long termId) {
        String uid = getAuthenticatedUid();
        TermResponseDTO term = studySetupService.getTermById(uid, termId);
        return (term == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(term);
    }

    // GET: Get current term based on current date
    @GetMapping("/terms/current")
    public ResponseEntity<TermResponseDTO> getCurrentTerm() {
        String uid = getAuthenticatedUid();
        TermResponseDTO term = studySetupService.getCurrentTerm(uid);
        return (term == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(term);
    }


    // POST: Save Term
    @PostMapping("/terms")
    public ResponseEntity<TermResponseDTO> saveTerm(@RequestBody TermRequestDTO termDTO) {
        String uid = getAuthenticatedUid();
        TermResponseDTO savedTerm = studySetupService.saveTerm(uid, termDTO);
        return ResponseEntity.ok(savedTerm);
    }

    @PutMapping("/terms/{id}")
    public ResponseEntity<TermResponseDTO> updateTerm(@PathVariable Long id, @RequestBody TermRequestDTO request) {
        String uid = getAuthenticatedUid();
        TermResponseDTO updatedTerm = studySetupService.updateTerm(uid, request, id);
        return ResponseEntity.ok(updatedTerm);
    }

    // POST: Save list of courses
    @PostMapping("/courses")
    public ResponseEntity<Void> saveCourses(@RequestBody List<CourseDTO> courseDTOs) {
        String uid = getAuthenticatedUid();
        studySetupService.saveCourses(uid, courseDTOs);
        return ResponseEntity.ok().build();
    }

    // POST: Save all course details (topics, exams, assignments)
    @PostMapping("/course-details")
    public ResponseEntity<Void> saveCourseDetails(@RequestBody List<CourseDTO> courseDTOs) {
        String uid = getAuthenticatedUid();
        studySetupService.saveCourseDetails(uid, courseDTOs);
        return ResponseEntity.ok().build();
    }

    // POST: Save availability list
    @PostMapping("/availabilities")
    public ResponseEntity<Void> saveAvailabilities(@RequestBody List<AvailabilityDTO> availabilityDTOs) {
        String uid = getAuthenticatedUid();
        studySetupService.saveAvailabilities(uid, availabilityDTOs);
        return ResponseEntity.ok().build();
    }

    private String getAuthenticatedUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}