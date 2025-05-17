package senior.project.service;

import senior.project.entity.User;

import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> findByUid(String uid);
    Optional<User> findByEmail(String email);
}