package cc.yezj.rpc.demo.provider;

import cc.yezj.rpc.core.test.TestZKServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RpcDemoProviderApplicationTests {

	static TestZKServer zkServer = new TestZKServer();

	@BeforeAll
	static void init() {
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		System.out.println(" =============     ZK2182    ========== ");
		System.out.println(" ====================================== ");
		System.out.println(" ====================================== ");
		zkServer.start();
	}

	@Test
	void contextLoads() {
		System.out.println(" ===> KkrpcDemoProviderApplicationTests  .... ");
	}

	@AfterAll
	static void destory() {
		zkServer.stop();
	}

}
