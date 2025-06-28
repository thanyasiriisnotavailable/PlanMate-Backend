package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;
import senior.project.enums.ExamType;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Exam {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Enumerated(EnumType.STRING)
    private ExamType type;

    private LocalDate date;
    private String startTime;
    private String endTime;

    @ManyToOne
    private Course course;
}
