package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.api.LoadBalancer;
import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.api.RpcContext;
import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.util.MethodUtil;
import cc.yezj.rpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConsumerInvocationHandler implements InvocationHandler {
    private Class<?> service;

    private RpcContext rpcContext;

    private List<String> providers;

    HttpInvoker httpInvoker = new OKHttpInvoker();

    public ConsumerInvocationHandler(Class<?> service, RpcContext rpcContext, List<String> providers) {
        this.service = service;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);
        System.out.println("request = " + request);

        List<String> urls = rpcContext.getRouter().route(providers);
        String url = (String) rpcContext.getLoadBalancer().choice(urls);
        System.out.println("loadBalancer.choice => "+url);

        //改成http请求
        RpcResponse<?> response = httpInvoker.post(request, url);
        System.out.println("response = " + response);
        if(response != null && response.isSuccess() && response.getData() != null){
//            if(response.getData() instanceof JSONObject){
//                return ((JSONObject) response.getData()).toJavaObject(method.getReturnType());
//            }
            return TypeUtils.cast(response.getData(), method.getReturnType());
        } else {
            if(response != null && response.getException() != null){
                throw new RuntimeException(response.getException());
            }
            return null;
        }
    }



}
