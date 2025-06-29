package senior.project.dto.plan;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratePlanResponseDTO {

    private String course;
    private String topic;
    private String title;
    private Integer duration;
    private String type; // "study", "assignment", "review"
    private String examDate; // format: "dd/MM/yyyy"
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private Integer slot_id;
}
