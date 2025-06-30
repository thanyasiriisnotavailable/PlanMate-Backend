package senior.project.service;

import jakarta.transaction.Transactional;
import senior.project.dto.plan.ScheduleDTO;
import senior.project.dto.plan.ScheduleViewDTO;
import senior.project.dto.plan.StudySetupDTO;

public interface ScheduleService {
    ScheduleViewDTO getSchedule();
    void saveSchedule(ScheduleDTO dto);

    @Transactional
    void updateSchedule(ScheduleDTO dto);

    ScheduleDTO generateScheduleFromFastAPI(StudySetupDTO setupDTO);
}
