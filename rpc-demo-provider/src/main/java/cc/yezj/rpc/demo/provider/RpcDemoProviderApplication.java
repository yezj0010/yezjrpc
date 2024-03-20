package cc.yezj.rpc.demo.provider;

import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.provider.ProviderBootStrap;
import cc.yezj.rpc.core.provider.ProviderConfig;
import cc.yezj.rpc.core.provider.ProviderInvoker;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@Import(ProviderConfig.class)
@RestController
public class RpcDemoProviderApplication {
	@Resource
	private ProviderInvoker providerInvoker;


	public static void main(String[] args) {
		SpringApplication.run(RpcDemoProviderApplication.class, args);
	}


	//使用HTTP + JSON 来实现序列化和通信
	@RequestMapping("/")
	public RpcResponse invoke(@RequestBody RpcRequest request) {
		return providerInvoker.invoke(request);
	}

	@Bean
	ApplicationRunner provideRunner(){
		return x -> {
//			RpcRequest request = new RpcRequest();
//			request.setService("cc.yezj.rpc.demo.api.UserService");
//			request.setMethod("findById");
//			request.setArgs(new Object[]{100});

//			request.setService("cc.yezj.rpc.demo.api.OrderService");
//			request.setMethodSign(OrderService.class.getMethod("queryOne", Long.class));
//			request.setArgs(new Object[]{10});
//			RpcResponse invoke = invoke(request);
//			System.out.printf("return " + invoke);
		};
	}
}
