package senior.project.dto.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.Exam;
import senior.project.enums.ExamType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDTO {
    private String id;
    private String generatedAt;
    private ExamType examType;
    private Long termId;

    @JsonProperty("study_plan")
    private List<SessionDTO> studyPlan;

    @JsonProperty("unscheduled_plan")
    private List<SessionDTO> unscheduledPlan;
}