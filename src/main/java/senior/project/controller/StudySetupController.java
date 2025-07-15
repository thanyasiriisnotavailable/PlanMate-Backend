package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.*;
import senior.project.dto.plan.StudySetupDTO;
import senior.project.service.StudySetupService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/study-setup")
@RequiredArgsConstructor
public class StudySetupController {

    private final StudySetupService studySetupService;

    @PostMapping
    public ResponseEntity<Void> setupStudyPlan(@RequestBody StudySetupDTO dto) {
        studySetupService.processStudySetup(dto);
        return ResponseEntity.ok().build();
    }

    // GET: Get full study setup
    @GetMapping
    public ResponseEntity<StudySetupDTO> getStudySetup() {
        StudySetupDTO dto = studySetupService.getStudySetup();
        return (dto == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    // GET: Get current term based on current date
    @GetMapping("/terms/current")
    public ResponseEntity<TermResponseDTO> getCurrentTerm() {
        TermResponseDTO term = studySetupService.getCurrentTerm();
        return (term == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(term);
    }

    // POST: Save Term (creates a new term)
    @PostMapping("/terms")
    public ResponseEntity<TermResponseDTO> createTerm(@RequestBody TermRequestDTO termDTO) {
        // For creation, the termId is null
        TermResponseDTO savedTerm = studySetupService.saveTerm(termDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTerm); // 201 Created
    }

    // PUT: Update Term
    @PutMapping("/terms/{termId}")
    public ResponseEntity<TermResponseDTO> updateTerm(@PathVariable Long termId, @RequestBody TermRequestDTO request) {
        try {
            // For update, we pass the termId from the path
            TermResponseDTO updatedTerm = studySetupService.saveTerm(request, termId);
            return ResponseEntity.ok(updatedTerm);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // PUT: Save or Update a list of courses for a specific term
    @PutMapping("/terms/{termId}/courses")
    public ResponseEntity<List<CourseResponseDTO>> saveAllCourses(
            @PathVariable Long termId,
            @RequestBody List<CourseResponseDTO> course) {
        List<CourseResponseDTO> savedCourses = studySetupService.saveAllCourses(termId, course);
        return ResponseEntity.ok(savedCourses);
    }

    // DELETE: Delete a specific course
    @DeleteMapping("/terms/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId) {
        try {
            studySetupService.deleteCourse(courseId);
            return ResponseEntity.noContent().build(); // 204 No Content for successful deletion
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/courses/details")
    public ResponseEntity<CourseResponseDTO> updateCourseDetails(
            @RequestBody CourseResponseDTO details) {
        CourseResponseDTO updated = studySetupService.updateCourseDetails(details);
        return ResponseEntity.ok(updated);
    }

    // GET: Get all availabilities for current user
    @GetMapping("/availabilities")
    public ResponseEntity<List<AvailabilityDTO>> getAvailabilities() {
        List<AvailabilityDTO> availabilities = studySetupService.getAvailabilities();
        return ResponseEntity.ok(availabilities);
    }

    // PUT: Save availability list
    @PutMapping("/availabilities")
    public ResponseEntity<List<AvailabilityDTO>> saveAvailabilities(@RequestBody List<AvailabilityRequestDTO> availabilityList) {
        List<AvailabilityDTO> availabilities = studySetupService.saveAvailabilities(availabilityList);
        return ResponseEntity.ok(availabilities);
    }
}