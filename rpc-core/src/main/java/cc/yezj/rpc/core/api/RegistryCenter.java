package cc.yezj.rpc.core.api;

import cc.yezj.rpc.core.meta.InstanceMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注册中心
 */
public interface RegistryCenter {

    void start();

    void stop();

    void register(String service, InstanceMeta instance);

    void unregister(String service, InstanceMeta instance);

    //consumer
    List<InstanceMeta> fetchAll(String serviceName);

    class StaticRegistryCenter implements RegistryCenter{

        List<String> providers;

        public StaticRegistryCenter(List<String> providers){
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(String service, InstanceMeta instance) {

        }

        @Override
        public void unregister(String service, InstanceMeta instance) {

        }

        @Override
        public List<InstanceMeta> fetchAll(String serviceName) {
            return providers.stream().map(i -> {
                String[] datas = i.split("_");
                return InstanceMeta.http(datas[0], Integer.valueOf(datas[1]));
            }).collect(Collectors.toList());
        }

        @Override
        public void subscribe(String service, ChangedListener changedListener) {

        }

        @Override
        public void heartbeat() {

        }
    }

    //TODO 比较复杂，需要监听zk变化
    void subscribe(String service, ChangedListener changedListener);

    //TODO 心跳检测
    void heartbeat();
}
