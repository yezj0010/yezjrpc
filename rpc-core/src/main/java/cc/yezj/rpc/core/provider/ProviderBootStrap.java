package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.core.model.request.RpcRequest;
import cc.yezj.rpc.core.model.request.RpcResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootStrap implements ApplicationContextAware {

    @Resource
    private ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void buildProviders(){
        Map<String, Object> providersMap = applicationContext.getBeansWithAnnotation(YezjProvider.class);
        providersMap.forEach((k,v) -> System.out.printf(k));
        providersMap.values().forEach(this::getInterface);
    }

    private void getInterface(Object x){
        Class<?>[] interfaces = x.getClass().getInterfaces();
        Class<?> anInterface = interfaces[0];
        skeleton.put(anInterface.getCanonicalName(), x);

    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = skeleton.get(request.getService());
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, 0, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if(method.getName().equals(methodName)) {  // 有多个重名方法，
                return method;
            }
        }
        return null;
    }

}
