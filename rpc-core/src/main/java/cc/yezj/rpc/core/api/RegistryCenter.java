package cc.yezj.rpc.core.api;

import cc.yezj.rpc.core.meta.InstanceMeta;
import cc.yezj.rpc.core.meta.ServiceMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注册中心
 */
public interface RegistryCenter {

    void start();

    void stop();

    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    //consumer
    List<InstanceMeta> fetchAll(ServiceMeta serviceName);

    //监听zk变化
    void subscribe(ServiceMeta service, ChangedListener changedListener);

    //TODO 心跳检测
    void heartbeat();

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
        public void register(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta serviceMeta) {
            return providers.stream().map(i -> {
                String[] datas = i.split("_");
                return InstanceMeta.http(datas[0], Integer.valueOf(datas[1]));
            }).collect(Collectors.toList());
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener changedListener) {

        }

        @Override
        public void heartbeat() {

        }
    }


}
