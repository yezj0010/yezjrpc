package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.model.request.RpcRequest;
import cc.yezj.rpc.core.model.request.RpcResponse;
import cc.yezj.rpc.core.util.MethodUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class ConsumerInvocationHandler implements InvocationHandler {

    private static final MediaType JSON_TYPE = MediaType.get("application/json;charset=utf-8");

    private Class<?> service;

    public ConsumerInvocationHandler(Class<?> service) {
        this.service = service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);
        //改成http请求
        RpcResponse response = post(request);
        if(response != null && response.isSuccess() && response.getData() != null){
            if(response.getData() instanceof JSONObject){
                return ((JSONObject) response.getData()).toJavaObject(method.getReturnType());
            }
            return response.getData();
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

    private RpcResponse post(RpcRequest request){
        String reqJson = JSON.toJSONString(request);
        Request httpRequest = new Request.Builder()
                .url("http://localhost:9001")
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
