package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.registry.ZkRegistryCenter;
import cc.yezj.rpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
@Import({SpringBootTransport.class})
public class ProviderConfig {

    @Bean
    ProviderBootStrap providerBootStrap() {
        return new ProviderBootStrap();
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootStrap providerBootStrap){
        return new ProviderInvoker(providerBootStrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    ApplicationRunner CreateProviderBootStrapRunner(@Autowired ProviderBootStrap providerBootStrap){
        return x -> {
            log.info("CreateProviderBootStrapRunner start,.....");
            providerBootStrap.start();
            log.info("CreateProviderBootStrapRunner end......");
        };
    }

    @Bean // (initMethod = "start", destroyMethod = "stop")
    RegistryCenter providerRc(){
        return new ZkRegistryCenter();
    }
}
