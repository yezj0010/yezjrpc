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

    private static final MediaType JSON_TYPE = MediaType.get("application/json;charset=utf-8");

    private Class<?> service;

    private RpcContext rpcContext;

    private List<String> providers;

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
        RpcResponse response = post(request, url);
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

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest request, String url){
        String reqJson = JSON.toJSONString(request);
        Request httpRequest = new Request.Builder()
                .url(url) //TODO 使用负载均衡替换
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        try{
            String result = client.newCall(httpRequest).execute().body().string();
            RpcResponse response = JSON.parseObject(result, RpcResponse.class);
            return response;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
