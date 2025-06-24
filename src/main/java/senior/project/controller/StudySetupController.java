package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.*;
import senior.project.service.StudySetupService;

import java.util.List;
import java.util.NoSuchElementException;

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
        try {
            TermResponseDTO term = studySetupService.getTermById(uid, termId);
            return ResponseEntity.ok(term);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }
    }

    // GET: Get current term based on current date
    @GetMapping("/terms/current")
    public ResponseEntity<TermResponseDTO> getCurrentTerm() {
        String uid = getAuthenticatedUid();
        TermResponseDTO term = studySetupService.getCurrentTerm(uid);
        return (term == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(term);
    }

    // POST: Save Term (creates a new term)
    @PostMapping("/terms")
    public ResponseEntity<TermResponseDTO> createTerm(@RequestBody TermRequestDTO termDTO) {
        String uid = getAuthenticatedUid();
        TermResponseDTO savedTerm = studySetupService.saveTerm(uid, termDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTerm); // 201 Created
    }

    // PUT: Update Term
    @PutMapping("/terms/{termId}")
    public ResponseEntity<TermResponseDTO> updateTerm(@PathVariable Long termId, @RequestBody TermRequestDTO request) {
        String uid = getAuthenticatedUid();
        try {
            TermResponseDTO updatedTerm = studySetupService.updateTerm(uid, request, termId);
            return ResponseEntity.ok(updatedTerm);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // PUT: Save or Update a list of courses for a specific term
    // This replaces the old /courses and /course-details for general course management
    @PutMapping("/terms/{termId}/courses")
    public ResponseEntity<List<CourseResponseDTO>> saveAllCourses(
            @PathVariable Long termId,
            @RequestBody List<CourseResponseDTO> course) {
        String uid = getAuthenticatedUid();
        try {
            List<CourseResponseDTO> savedCourses = studySetupService.saveAllCourses(uid, termId, course);
            return ResponseEntity.ok(savedCourses);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE: Delete a specific course
    @DeleteMapping("/terms/{termId}/courses/{courseCode}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long termId,
            @PathVariable String courseCode) {
        String uid = getAuthenticatedUid();
        try {
            studySetupService.deleteCourse(uid, termId, courseCode);
            return ResponseEntity.noContent().build(); // 204 No Content for successful deletion
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    // POST: Save availability list
    // Consider if this should also be a PUT for updates, or if it's always an overwrite.
//    @PostMapping("/availabilities")
//    public ResponseEntity<Void> saveAvailabilities(@RequestBody List<AvailabilityDTO> availabilityDTOs) {
//        String uid = getAuthenticatedUid();
//        try {
//            studySetupService.saveAvailabilities(uid, availabilityDTOs);
//            return ResponseEntity.ok().build();
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.badRequest().build(); // User not found, or similar
//        }
//    }
//
    private String getAuthenticatedUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}