package senior.project.dao;

import senior.project.entity.User;

import java.util.Optional;

public interface UserDao {
    User save(User user);
    User findByUid(String uid);
    Optional<User> findByEmail(String email);
}
