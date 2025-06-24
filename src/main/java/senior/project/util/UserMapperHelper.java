package senior.project.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import senior.project.dao.UserDao;
import senior.project.entity.User;

@Component
@RequiredArgsConstructor
public class UserMapperHelper {

    private final UserDao userDao;

    public User resolveUser(String userUid) {
        return userDao.findByUid(userUid);
    }
}