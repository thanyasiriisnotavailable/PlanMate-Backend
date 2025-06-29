package senior.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import senior.project.enums.ExamType;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamDTO {
    private String id;
    private ExamType type;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date; // e.g., "2025-02-25"

    private Long courseId;
}