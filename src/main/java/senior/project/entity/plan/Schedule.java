package senior.project.entity.plan;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import senior.project.entity.Term;
import senior.project.entity.User;
import senior.project.enums.ExamType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    private String id;

    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @ManyToOne
    private User user;

    @ManyToOne
    private Term term;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Session> sessions;
}