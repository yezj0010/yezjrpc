package cc.yezj.rpc.demo.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.demo.api.Order;
import cc.yezj.rpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@YezjProvider
@Component
public class OrderServiceImpl implements OrderService {

    @Override
    public String test(long id) {
        if(id ==404){
            throw new RuntimeException("参数错误，404");
        }
        return """
                 hha 
                 dfadf
                 dfaf
                 next line!
                 """;
    }

    @Override
    public Order queryOne(long id) {
        return new Order(id, new BigDecimal(new Random().nextInt(9999)));
    }
}
