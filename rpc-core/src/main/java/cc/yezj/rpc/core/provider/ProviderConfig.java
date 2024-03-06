package cc.yezj.rpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootStrap providerBootStrap() {
        return new ProviderBootStrap();
    }
}
