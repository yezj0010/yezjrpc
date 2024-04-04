package cc.yezj.rpc.core.api;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {

    private List<Filter> filters;
    private Router router;
    private LoadBalancer loadBalancer;

    private Map<String, String> parameters = new HashMap<>();
    //color == gray  染色标识
    //global_trace_id  全局ID
    //其他变量gw，通过这个传递 -> service 1 -> service2(可能还要跨线程传递) 。。。
    //http headers ,, 如果用的http通信，还可以传递http头的属性

    public static ThreadLocal<Map<String,String>> ContextParameters = ThreadLocal.withInitial(() -> new HashMap<>());

    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }
}
