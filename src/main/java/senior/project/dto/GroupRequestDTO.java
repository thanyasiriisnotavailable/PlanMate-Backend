package senior.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequestDTO {
    private String groupName;
    private String imageUrl;
}
