package senior.project.dto;

import lombok.*;
import senior.project.entity.CourseId;
import senior.project.enums.ExamType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamDTO {
    private String id;
    private ExamType type;
    private LocalDate date; // e.g., "2025-02-25"
    private String startTime;
    private String endTime;

    private CourseId courseId;
}
