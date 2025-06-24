package senior.project.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CourseBaseDTO {
    private String courseCode;
    private String name;
    private Long credit;
}
