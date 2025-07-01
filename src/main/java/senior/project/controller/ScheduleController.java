package senior.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.ScheduleViewDTO;
import senior.project.dto.plan.StudySetupDTO;
import senior.project.service.ScheduleService;
import senior.project.service.StudySetupService;

import java.util.Map;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final StudySetupService studySetupService;

    @GetMapping
    public ResponseEntity<ScheduleDTO> getSchedule() {
        ScheduleDTO dto = scheduleService.getSchedule();

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ScheduleDTO> saveSchedule(@RequestBody ScheduleDTO dto) {
        ScheduleDTO savedSchedule = scheduleService.saveSchedule(dto);
        return ResponseEntity.ok(savedSchedule);
    }

    @PutMapping("/schedule/{id}")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable String id, @RequestBody ScheduleDTO dto) {
        dto.setId(id); // Set the path param into the DTO
        ScheduleDTO updatedSchedule = scheduleService.updateSchedule(dto);
        return ResponseEntity.ok(updatedSchedule);
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

        return ResponseEntity.ok(generated);
    }
}