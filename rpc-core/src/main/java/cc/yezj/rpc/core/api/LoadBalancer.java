package cc.yezj.rpc.core.api;

import java.util.List;

/**
 * 真正常见的负载均衡算法，
 * 权重轮询
 * AAWR自适应：根据响应时间来动态分配权重
 *
 * avg*0.3 + last*0.7 = 最终的权重。
 *
 */
public interface LoadBalancer<T> {

    T choice(List<T> providers);

    LoadBalancer Default = p -> p == null || p.isEmpty() ? null : p.get(0);
}
