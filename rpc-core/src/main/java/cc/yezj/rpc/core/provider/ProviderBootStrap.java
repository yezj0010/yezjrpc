package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.annotation.YezjProvider;
import cc.yezj.rpc.core.meta.ProviderMeta;
import cc.yezj.rpc.core.model.request.RpcRequest;
import cc.yezj.rpc.core.model.request.RpcResponse;
import cc.yezj.rpc.core.util.MethodUtil;
import cc.yezj.rpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class ProviderBootStrap implements ApplicationContextAware {

    @Resource
    private ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();//一个key多个值

    @PostConstruct
    public void start(){
        Map<String, Object> providersMap = applicationContext.getBeansWithAnnotation(YezjProvider.class);
        providersMap.forEach((k,v) -> System.out.printf(k));
        providersMap.values().forEach(this::getInterface);
    }

    private void getInterface(Object x){
        Class<?>[] interfaces = x.getClass().getInterfaces();//兼容实现了多个接口
        Arrays.stream(interfaces).forEach(anInterface -> {
            Method[] methods = anInterface.getMethods();
            for(Method method : methods){
                if(MethodUtil.checkLocalMethod(method)){
                    continue;
                }
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
