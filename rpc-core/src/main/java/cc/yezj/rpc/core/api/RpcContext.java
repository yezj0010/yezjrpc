package cc.yezj.rpc.core.api;

import lombok.Data;

import java.util.List;

@Data
public class RpcContext {

    private List<Filter> filters;
    private Router router;
    private LoadBalancer loadBalancer;
}
