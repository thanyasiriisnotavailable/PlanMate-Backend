package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Table(name = "study_groups")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String imageUrl;

    @Column(unique = true)
    private String joinCode;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members;
}