package cc.yezj.rpc.core.provider;

import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.meta.ProviderMeta;
import cc.yezj.rpc.core.util.TypeUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

/**
 * 调用provider，职责分离
 */
public class ProviderInvoker {

    /**
     * 接口全限定名 - List<ProviderMeta>
     */
    private MultiValueMap<String, ProviderMeta> skeleton = null;

    public ProviderInvoker(ProviderBootStrap providerBootStrap){
        this.skeleton = providerBootStrap.getSkeleton();
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
}
