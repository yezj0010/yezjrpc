package cc.yezj.rpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

    @Bean
    ConsumerBootStrap getConsumerBootStrap(){
        return new ConsumerBootStrap();
    }

    /**
     * spring启动好之后，才会执行ApplicationRunner的方法
     * TODO ,后续可以改成其他方式
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner CreateConsumerProxy(@Autowired ConsumerBootStrap consumerBootStrap){
        return x -> {
//            System.out.println("CreateConsumerProxy start,.....");
            consumerBootStrap.start();
//            System.out.println("CreateConsumerProxy end......");
        };
    }
}
