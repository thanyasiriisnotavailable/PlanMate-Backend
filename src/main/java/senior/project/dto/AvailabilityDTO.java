package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityDTO {
    private String userUid;
    private List<AvailabilitySlot> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailabilitySlot {
        private String dayOfWeek;
        private String startTime;
        private String endTime;
    }
}
