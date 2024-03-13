package cc.yezj.rpc.demo.api;

public interface UserService {

    User findById(long userId);

    User findById(int userId);

    User findById(long userId, String name);
}
