package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.meta.ProviderMeta;
import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.util.MethodUtil;
import cc.yezj.rpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服务提供者启动类
 */
@Data
public class ProviderBootStrap implements ApplicationContextAware {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 接口全限定名 - List<ProviderMeta>
     */
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();//一个key多个值

    @Value("${server.port}")
    private String port;

    private String instance;

    @PostConstruct
    public void init(){
        Map<String, Object> providersMap = applicationContext.getBeansWithAnnotation(YezjProvider.class);
        providersMap.forEach((k,v) -> System.out.printf(k));
        providersMap.values().forEach(this::getInterface);
    }

    @SneakyThrows
    public void start(){//放到ApplicationRunner的时候执行，才能保证服务正常启动之后才进行注册，否则客户端调用会报错服务问题
        //注册到zookeeper
        String ip = InetAddress.getLocalHost().getHostAddress();
        this.instance = ip + "_" + port;
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop(){
        skeleton.keySet().forEach(this::unRegisterService);
    }

    private void unRegisterService(String serviceName) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.unregister(serviceName, this.instance);
    }

    private void registerService(String serviceName){
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
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

    public RpcResponse invoke(RpcRequest request) {
        String methodSign = request.getMethodSign();
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());

        try {
            ProviderMeta meta = findProviderMeta(providerMetas, methodSign);
            Object args[] = processArgs(request.getArgs(), meta.getMethod().getParameterTypes());
            Object result = meta.getMethod().invoke(meta.getServiceImpl(), args);
            return new RpcResponse(true, 0, result, null);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            rpcResponse.setException(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            rpcResponse.setException(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if(args == null){
            return null;
        }
        for(int i=0;i<args.length;i++){
            args[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return args;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        if(CollectionUtils.isEmpty(providerMetas)){
            return null;
        }
        Optional<ProviderMeta> first = providerMetas.stream().filter(i -> i.getMethodSign().equals(methodSign)).findFirst();
        return first.orElse(null);
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
