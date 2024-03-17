package cc.yezj.rpc.core.cluster;

import cc.yezj.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRibonLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger(0);

    @Override
    public T choice(List<T> providers) {
        if(providers == null || providers.isEmpty()){
            return null;
        }
        if(providers.size() == 1) {
            return providers.get(0);
        }
        return providers.get((index.getAndIncrement()&0x7fffffff) % providers.size());
    }
}
