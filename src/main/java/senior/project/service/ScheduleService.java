package senior.project.service;

import jakarta.transaction.Transactional;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.StudySetupDTO;

public interface ScheduleService {
    ScheduleDTO getSchedule();
    ScheduleDTO saveSchedule(ScheduleDTO dto);

    @Transactional
    ScheduleDTO updateSchedule(ScheduleDTO dto);

    ScheduleDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO);
}
