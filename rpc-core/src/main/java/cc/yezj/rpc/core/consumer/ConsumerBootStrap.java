package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.annotation.YezjConsumer;
import cc.yezj.rpc.core.api.ChangedListener;
import cc.yezj.rpc.core.api.LoadBalancer;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.api.RpcContext;
import cc.yezj.rpc.core.meta.InstanceMeta;
import cc.yezj.rpc.core.meta.ServiceMeta;
import cc.yezj.rpc.core.registry.Event;
import cc.yezj.rpc.core.util.MethodUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ConsumerBootStrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

    @Value("${yezjrpc.app.id}")
    private String app;

    @Value("${yezjrpc.app.namespace}")
    private String namespace;

    @Value("${yezjrpc.app.env}")
    private String env;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){

        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RpcContext rpcContext = new RpcContext();
        rpcContext.setFilters(null);
        rpcContext.setRouter(router);
        rpcContext.setLoadBalancer(loadBalancer);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanDefinitionNames) {
            Object bean = applicationContext.getBean(name);
            List<Field> annotatedField = MethodUtil.findAnnotatedField(bean.getClass(), YezjConsumer.class);
            annotatedField.stream().forEach(i -> {
                try{
                    Class<?> type = i.getType();
                    String classFullName = type.getCanonicalName();//拿到全限定名称
                    Object o = stub.get(classFullName);//获取代理类，判断是否存在
                    if (o == null) {
                        o = createFromRegistry(type, rpcContext, rc);
                        stub.put(classFullName, o);
                    }
                    i.setAccessible(true);
                    i.set(bean, o);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    private Object createFromRegistry(Class<?> type, RpcContext rpcContext, RegistryCenter rc) {
        String serviceName = type.getCanonicalName();
        ServiceMeta serviceMeta = ServiceMeta.builder().name(serviceName).app(app).namespace(namespace).env(env).build();
        List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        rc.subscribe(serviceMeta, new ChangedListener() {
            @Override
            public void fire(Event event) {
                providers.clear();
                providers.addAll(event.getData());
            }
        });

        return createConsumer(type, rpcContext, providers);
    }

    private List<String> mapUrl(List<InstanceMeta> nodes){
        return nodes.stream().map(i -> "http://" + i.getHost()+":"+i.getPort() + "/").collect(Collectors.toList());//“/”加不加都可以
    }

    private Object createConsumer(Class<?> serviceClass, RpcContext rpcContext, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass}, new ConsumerInvocationHandler(serviceClass, rpcContext, providers));
    }
}
