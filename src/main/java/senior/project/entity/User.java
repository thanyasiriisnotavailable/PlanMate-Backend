package senior.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String uid;  // Firebase UID

    @Column(nullable = false, unique = true)
    private String email;

    private String displayName;

    private String profileImage;
}
