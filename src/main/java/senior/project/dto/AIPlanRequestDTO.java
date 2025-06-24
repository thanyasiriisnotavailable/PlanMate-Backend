package senior.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIPlanRequestDTO {
    private StudyPreferenceDTO studyPreference;
    private List<AvailabilityDTO> availabilities;
    private List<CourseResponseDTO> courses;
    private List<TopicDTO> topics;
    private List<AssignmentDTO> assignments;
    private List<ExamDTO> exams;
}
