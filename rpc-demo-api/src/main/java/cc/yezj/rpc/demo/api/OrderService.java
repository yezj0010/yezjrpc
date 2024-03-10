package cc.yezj.rpc.demo.api;

public interface OrderService {

    public String test(long id);

    Order queryOne(long id);
}
