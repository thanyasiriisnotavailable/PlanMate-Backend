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

    @GetMapping
    public ResponseEntity<StudySetupDTO> getStudySetup() {
        StudySetupDTO dto = studySetupService.getStudySetup();
        return (dto == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @GetMapping("/terms/current")
    public ResponseEntity<TermResponseDTO> getCurrentTerm() {
        TermResponseDTO term = studySetupService.getCurrentTerm();
        return (term == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(term);
    }

    @PostMapping("/terms")
    public ResponseEntity<TermResponseDTO> createTerm(@RequestBody TermRequestDTO termDTO) {
        TermResponseDTO savedTerm = studySetupService.saveTerm(termDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTerm);
    }

    @PutMapping("/terms/{termId}")
    public ResponseEntity<TermResponseDTO> updateTerm(@PathVariable Long termId, @RequestBody TermRequestDTO request) {
        try {
            TermResponseDTO updatedTerm = studySetupService.saveTerm(request, termId);
            return ResponseEntity.ok(updatedTerm);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/terms/{termId}/courses")
    public ResponseEntity<List<CourseResponseDTO>> saveAllCourses(
            @PathVariable Long termId,
            @RequestBody List<CourseResponseDTO> course) {
        List<CourseResponseDTO> savedCourses = studySetupService.saveAllCourses(termId, course);
        return ResponseEntity.ok(savedCourses);
    }

    @DeleteMapping("/terms/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId) {
        try {
            studySetupService.deleteCourse(courseId);
            return ResponseEntity.noContent().build();
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

    @GetMapping("/availabilities")
    public ResponseEntity<List<AvailabilityDTO>> getAvailabilities() {
        List<AvailabilityDTO> availabilities = studySetupService.getAvailabilities();
        return ResponseEntity.ok(availabilities);
    }

    @PutMapping("/availabilities")
    public ResponseEntity<List<AvailabilityDTO>> saveAvailabilities(@RequestBody List<AvailabilityRequestDTO> availabilityList) {
        List<AvailabilityDTO> availabilities = studySetupService.saveAvailabilities(availabilityList);
        return ResponseEntity.ok(availabilities);
    }
}