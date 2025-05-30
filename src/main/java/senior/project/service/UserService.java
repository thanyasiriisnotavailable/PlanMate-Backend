package senior.project.service;

import senior.project.entity.User;

import java.util.Optional;

public interface UserService {
    User save(User user);
    User findByUid(String uid);;
}