package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.StudySetupDTO;
import senior.project.service.ScheduleService;
import senior.project.service.StudySetupService;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final StudySetupService studySetupService;

    @GetMapping
    public ResponseEntity<ScheduleDTO> getSchedule() {
        ScheduleDTO dto = scheduleService.getScheduleForUser();
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<Void> saveSchedule(@RequestBody ScheduleDTO dto) {
        scheduleService.saveSchedule(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<ScheduleDTO> generateScheduleFromFastAPI() {
        // Load the existing StudySetupDTO from DB
        StudySetupDTO setupDTO = studySetupService.getStudySetup();
        if (setupDTO == null) {
            return ResponseEntity.badRequest().build();
        }

        // Call FastAPI microservice to get schedule
        ScheduleDTO generated = scheduleService.generateScheduleFromFastAPI(setupDTO);
        if (generated == null) {
            return ResponseEntity.status(502).build(); // Bad Gateway if FastAPI fails
        }

        // Save schedule to database
        scheduleService.saveSchedule(generated);
        return ResponseEntity.ok(generated);
    }
}