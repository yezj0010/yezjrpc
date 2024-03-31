package cc.yezj.rpc.core.filter;

import cc.yezj.rpc.core.api.Filter;
import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Order(Integer.MAX_VALUE) // 缓存的filter最后一个走，避免其他filter没生效
public class LocalCacheFilter implements Filter {


    /**
     * 优化
     * 1.可以更改为guava的cache 加容量和过期时间，
     * 2.
     */
    static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        cache.putIfAbsent(request.toString(), result);
        return result;
    }
}
