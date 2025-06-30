package senior.project.service;

import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.StudySetupDTO;

public interface ScheduleService {
    ScheduleDTO getScheduleForUser();
    void saveSchedule(ScheduleDTO dto);
    ScheduleDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO);
}
