package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.api.Filter;
import cc.yezj.rpc.core.api.LoadBalancer;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.cluster.RoundRibonLoadBalancer;
import cc.yezj.rpc.core.filter.LocalCacheFilter;
import cc.yezj.rpc.core.registry.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
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
            log.info("CreateConsumerProxy start,.....");
            consumerBootStrap.start();
            log.info("CreateConsumerProxy end......");
        };
    }

    @Bean
    public LoadBalancer loadBalancer(){
//        return new RandomLoadBalancer();
        return new RoundRibonLoadBalancer();
    }

    @Bean
    public Router router(){
        return Router.Default;
    }

    @Bean
    public Filter filter(){
        return new LocalCacheFilter();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRC(){
        return new ZkRegistryCenter();
    }
}
