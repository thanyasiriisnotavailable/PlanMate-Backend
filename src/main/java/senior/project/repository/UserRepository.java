package senior.project.repository;

import senior.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUid(String uid);
    Optional<User> findByEmail(String email);
}