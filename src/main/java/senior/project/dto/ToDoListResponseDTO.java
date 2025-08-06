package senior.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.dto.plan.SessionDTO;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToDoListResponseDTO {
    private List<SessionDTO> today;
    private List<SessionDTO> tomorrow;
    private List<SessionDTO> upcoming;
}