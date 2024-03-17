package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.consumer.ConsumerBootStrap;
import cc.yezj.rpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootStrap providerBootStrap() {
        return new ProviderBootStrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner CreateProviderBootStrapRunner(@Autowired ProviderBootStrap providerBootStrap){
        return x -> {
            System.out.println("CreateProviderBootStrapRunner start,.....");
            providerBootStrap.start();
            System.out.println("CreateProviderBootStrapRunner end......");
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    RegistryCenter providerRc(){
        return new ZkRegistryCenter();
    }
}
