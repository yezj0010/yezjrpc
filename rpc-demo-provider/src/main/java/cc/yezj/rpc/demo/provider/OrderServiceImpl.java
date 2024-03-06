package cc.yezj.rpc.demo.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.demo.api.OrderService;
import org.springframework.stereotype.Service;

@YezjProvider
@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public String test() {
        return """
                 hha 
                 dfadf
                 dfaf
                 next line!
                 """;
    }
}
