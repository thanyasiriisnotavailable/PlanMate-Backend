package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.enums.ExamType;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private ExamType type;

    private LocalDate date;
    private String startTime;
    private String endTime;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "term_id"),
            @JoinColumn(name = "course_code")
    })
    private Course course;
}
