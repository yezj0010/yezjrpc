package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.meta.InstanceMeta;
import cc.yezj.rpc.core.meta.ProviderMeta;
import cc.yezj.rpc.core.util.MethodUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 服务提供者启动类
 */
@Data
public class ProviderBootStrap implements ApplicationContextAware {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private RegistryCenter rc;

    /**
     * 接口全限定名 - List<ProviderMeta>
     */
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();//一个key多个值

    @Value("${server.port}")
    private String port;

    private InstanceMeta instance;

    @PostConstruct
    public void init(){
        Map<String, Object> providersMap = applicationContext.getBeansWithAnnotation(YezjProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        providersMap.forEach((k,v) -> System.out.printf(k));
        providersMap.values().forEach(this::getInterface);
    }

    @SneakyThrows
    public void start(){//放到ApplicationRunner的时候执行，才能保证服务正常启动之后才进行注册，否则客户端调用会报错服务问题
        //注册到zookeeper
        String ip = InetAddress.getLocalHost().getHostAddress();
        this.instance = InstanceMeta.http(ip, Integer.valueOf(port));
        rc.start();//注意顺序，先启动，再注册
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop(){
        skeleton.keySet().forEach(this::unRegisterService);
        rc.stop();//注意顺序，先取消注册，再停止
    }

    private void unRegisterService(String serviceName) {
        rc.unregister(serviceName, this.instance);
    }

    private void registerService(String serviceName){
        rc.register(serviceName, this.instance);
    }

    private void getInterface(Object x){
        Class<?>[] interfaces = x.getClass().getInterfaces();//兼容实现了多个接口
        Arrays.stream(interfaces).forEach(anInterface -> {
            Method[] methods = anInterface.getMethods();
            for(Method method : methods){
                if(MethodUtil.checkLocalMethod(method)){
                    continue;
                }
                //anInterface指的是带有@YezjProvider注解的类实现的接口，
                //x指的是带有@YezjProvider注解的实例
                //method是这个实例中的一个方法，
                //最终是一个实例 对应 多个它的方法，存储在multiMap中。multiMap其实存储的是一个list对象。
                createProvider(anInterface, x, method);
            }
        });

    }

    private void createProvider(Class<?> anInterface, Object x, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setMethodSign(MethodUtil.methodSign(method));
        providerMeta.setServiceImpl(x);
        System.out.println("create providerMeta="+providerMeta);
        skeleton.add(anInterface.getCanonicalName(), providerMeta);
    }


    @Deprecated
    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if(method.getName().equals(methodName)) {  // TODO 考虑有多个重名方法，
                return method;
            }
        }
        return null;
    }

}
