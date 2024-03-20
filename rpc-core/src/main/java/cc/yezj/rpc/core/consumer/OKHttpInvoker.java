package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.concurrent.TimeUnit;

public class OKHttpInvoker implements HttpInvoker{

    private static final MediaType JSON_TYPE = MediaType.get("application/json;charset=utf-8");

    OkHttpClient client = null;
    public OKHttpInvoker(){
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    public RpcResponse<Object> post(RpcRequest rpcRequest, String url){
        String reqJson = JSON.toJSONString(rpcRequest);
        Request httpRequest = new Request.Builder()
                .url(url)
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
