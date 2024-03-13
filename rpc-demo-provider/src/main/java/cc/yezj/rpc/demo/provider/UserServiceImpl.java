package cc.yezj.rpc.demo.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.demo.api.User;
import cc.yezj.rpc.demo.api.UserService;
import org.springframework.stereotype.Service;

@Service
@YezjProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(long userId) {
        User user = new User();
        user.setId(userId);
        user.setName("one, yezj");
        return user;
    }

    @Override
    public User findById(int userId) {
        User user = new User();
        user.setId(userId);
        user.setName("two, yezj");
        return user;
    }

    @Override
    public User findById(long userId, String name) {
        User user = new User();
        user.setId(userId);
        user.setName("three, "+name);
        return user;
    }

    @Override
    public long getId(long id) {
        return 1231331L;
    }

    @Override
    public long getId(User user) {
        return user.getId();
    }

    @Override
    public int[] getIds() {
        return new int[]{5,6,7,8};
    }

    @Override
    public long[] getLgIds() {
        return new long[]{1L,2L,3L};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }
}
