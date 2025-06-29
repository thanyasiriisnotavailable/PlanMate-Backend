package senior.project.service;

import senior.project.dto.plan.GeneratePlanResponseDTO;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.StudySetupDTO;

public interface ScheduleService {
    ScheduleDTO getScheduleForUser();
    void saveSchedule(GeneratePlanResponseDTO dto);
    GeneratePlanResponseDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO);
}
