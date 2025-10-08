package senior.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import senior.project.dao.UserDao;
import senior.project.entity.User;
import senior.project.service.UserService;
import org.springframework.stereotype.Service;

@Slf4j
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

    @Override
    public void updateFcmToken(String uid, String token) {
        User user = userDao.findByUid(uid);
        user.setFcmToken(token);
        userDao.save(user);
        log.debug("Updated FCM token in DB for uid={}", uid);
    }

    @Override
    public String getFcmToken(String uid) {
        User user = userDao.findByUid(uid);
        return user.getFcmToken();
    }
}