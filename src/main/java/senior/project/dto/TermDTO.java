package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermDTO {
    private Long id;
    private String name;
    private String userUid;
    private List<CourseDTO> courses;
}
