package senior.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupResponseDTO {
    private long id;
    private String name;
    private String imageUrl;
    private String joinCode;
    private List<GroupMemberDTO> members;
}
