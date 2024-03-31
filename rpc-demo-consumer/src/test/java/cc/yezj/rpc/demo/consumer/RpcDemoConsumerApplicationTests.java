package cc.yezj.rpc.demo.consumer;

import cc.yezj.rpc.demo.provider.RpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class RpcDemoConsumerApplicationTests {

    static ApplicationContext context;

    @BeforeAll
    static void init(){
        context = SpringApplication.run(RpcDemoProviderApplication.class, "--server.port=9104");
    }

    @Test
    void contextLoads() {
        System.out.println("consumer start .....");
    }


    @AfterAll
    static void destroy(){
        SpringApplication.exit(context, () -> 1);
    }

}
