package senior.project.service;

import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.ScheduleViewDTO;
import senior.project.dto.plan.StudySetupDTO;

public interface ScheduleService {
    ScheduleViewDTO getSchedule();
    void saveSchedule(ScheduleDTO dto);
    ScheduleDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO);
}
