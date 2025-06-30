package senior.project.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleViewDTO {
    List<SessionViewDTO> studyPlan;
    List<SessionViewDTO> unscheduledPlan;
}
