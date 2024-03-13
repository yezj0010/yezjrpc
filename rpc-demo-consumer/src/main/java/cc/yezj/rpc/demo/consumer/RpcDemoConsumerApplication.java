package cc.yezj.rpc.demo.consumer;

import cc.yezj.rpc.core.annotation.YezjConsumer;
import cc.yezj.rpc.core.consumer.ConsumerConfig;
import cc.yezj.rpc.demo.api.Order;
import cc.yezj.rpc.demo.api.OrderService;
import cc.yezj.rpc.demo.api.User;
import cc.yezj.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class RpcDemoConsumerApplication {

    @YezjConsumer
    private UserService userService;

    @YezjConsumer
    private OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(RpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumerRunner(){
        System.out.printf("execute consumerRunner");
        return x -> {
            User user = userService.findById(1L);
            System.out.println(user);
            User user2 = userService.findById(2, "llllll");
            System.out.println(user2);
//            Order order = orderService.queryOne(202);
//            System.out.println(order);
//            String test = orderService.test(404);
//            System.out.println(test);
        };
    }

}
