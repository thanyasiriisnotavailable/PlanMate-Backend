package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import senior.project.dao.UserDao;
import senior.project.entity.User;
import senior.project.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public User save(User user) {
        return userDao.save(user);
    }

    @Override
    public User findByUid(String uid) {
        return userDao.findByUid(uid);
    }
}