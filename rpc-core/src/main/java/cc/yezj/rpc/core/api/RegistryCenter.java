package cc.yezj.rpc.core.api;

import java.util.List;

/**
 * 注册中心
 */
public interface RegistryCenter {

    void start();

    void stop();

    void register(String service, String instance);

    void unregister(String service, String instance);

    //consumer
    List<String> fetchAll(String serviceName);

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
        public void register(String service, String instance) {

        }

        @Override
        public void unregister(String service, String instance) {

        }

        @Override
        public List<String> fetchAll(String serviceName) {
            return providers;
        }

        @Override
        public void subscribe() {

        }
    }

    void subscribe();//TODO 比较复杂，需要监听zk变化

    //TODO 心跳检测

}
