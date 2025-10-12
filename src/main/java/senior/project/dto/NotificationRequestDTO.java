package senior.project.dto;
import lombok.*;
import senior.project.enums.NotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {
    private String title;
    private String content;
    private NotificationType type;
}
