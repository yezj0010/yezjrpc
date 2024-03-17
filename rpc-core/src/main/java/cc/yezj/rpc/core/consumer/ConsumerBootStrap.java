package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.annotation.YezjConsumer;
import cc.yezj.rpc.core.api.ChangedListener;
import cc.yezj.rpc.core.api.LoadBalancer;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.api.RpcContext;
import cc.yezj.rpc.core.registry.Event;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ConsumerBootStrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

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
            List<Field> annotatedField = findAnnotatedField(bean.getClass());
            annotatedField.stream().forEach(i -> {
                try{
                    Class<?> type = i.getType();
                    String classFullName = type.getCanonicalName();//拿到全限定名称
                    Object o = stub.get(classFullName);//获取代理类，判断是否存在
                    if (o == null) {
                        o = createFromRegistry(type, rpcContext, rc);
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
        List<String> providers = mapUrl(rc.fetchAll(serviceName));
        rc.subscribe(serviceName, new ChangedListener() {
            @Override
            public void fire(Event event) {
                providers.clear();
                providers.addAll(mapUrl(event.getData()));
            }
        });

        return createConsumer(type, rpcContext, providers);
    }

    private List<String> mapUrl(List<String> nodes){
        return nodes.stream().map(i -> "http://" + i.replace("_", ":") + "/").collect(Collectors.toList());//“/”加不加都可以
    }

    private Object createConsumer(Class<?> serviceClass, RpcContext rpcContext, List<String> providers) {
        return Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass}, new ConsumerInvocationHandler(serviceClass, rpcContext, providers));
    }

    private List<Field> findAnnotatedField(Class<?> aClass) {
        List<Field> result = new ArrayList<>();
        while(aClass != null){
            Field[] declaredFields = aClass.getDeclaredFields();
            for(Field ff : declaredFields){
                if(ff.isAnnotationPresent(YezjConsumer.class)){
                    result.add(ff);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }
}
