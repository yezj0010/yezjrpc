package cc.yezj.rpc.demo.provider;

import cc.yezj.rpc.core.provider.ProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@Import(ProviderConfig.class)
@RestController
public class RpcDemoProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RpcDemoProviderApplication.class, args);
	}

}
