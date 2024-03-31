package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.api.Filter;
import cc.yezj.rpc.core.api.LoadBalancer;
import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.api.RpcContext;
import cc.yezj.rpc.core.api.RpcException;
import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.meta.InstanceMeta;
import cc.yezj.rpc.core.util.MethodUtil;
import cc.yezj.rpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConsumerInvocationHandler implements InvocationHandler {
    private Class<?> service;

    private RpcContext rpcContext;

    private List<InstanceMeta> providers;

    HttpInvoker httpInvoker = new OKHttpInvoker();

    public ConsumerInvocationHandler(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
        this.service = service;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtil.checkLocalMethod(method)) {
            return null;
        }

        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);
        log.debug("request = " + request);

        for (Filter filter : rpcContext.getFilters()) {
            Object preResult = filter.preFilter(request);
            if(preResult != null){
                log.debug(filter.getClass().getName() + ", preFilterResult:"+preResult);
                return preResult;
            }
        }

        List<String> nodes = rpcContext.getRouter().route(providers);
        InstanceMeta instanceMeta = (InstanceMeta) rpcContext.getLoadBalancer().choice(nodes);
        log.debug("loadBalancer.choice => "+instanceMeta);

        //改成http请求
        RpcResponse<?> response = httpInvoker.post(request, instanceMeta.getUrl());
        log.debug("response = " + response);

        Object result = castReturnResult(method, response);

        //调用后过滤
        for (Filter filter : rpcContext.getFilters()) {
            Object filterResult = filter.postFilter(request, response, result);
            if(filterResult != null){
                return filterResult;//TODO 这里逻辑好像有问题，一旦一个过滤器生效了，后面的过滤器就没意义了。
            }
        }

        return result;
    }

    @Nullable
    private static Object castReturnResult(Method method, RpcResponse<?> response) {
        if(response != null && response.isSuccess() && response.getData() != null){
            return TypeUtils.castMethodResult(method, response.getData());
        } else {
            if(response != null && response.getException() != null){
                if(response.getException() instanceof RpcException exception){
                    throw exception;
                }
                throw new RpcException(response.getException(), RpcException.INTER_ERROR_EX);
            }
            return null;
        }
    }


}
