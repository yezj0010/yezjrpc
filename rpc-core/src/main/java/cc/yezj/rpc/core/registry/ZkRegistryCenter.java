package cc.yezj.rpc.core.registry;

import cc.yezj.rpc.core.api.ChangedListener;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.meta.InstanceMeta;
import cc.yezj.rpc.core.meta.ServiceMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${yezjrpc.zkServer}")
    private String servers;

    @Value("${yezjrpc.zkNamespace}")
    private String namespace;

    private CuratorFramework client = null;

    @Override
    public void start() {
        //backoff每次去探测的时间间隔是上一次的2倍
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)//TODO 不能写死，待重构
                .namespace("yezjrpc")
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ====> zk client start, server="+servers+", namespace="+namespace);
        client.start();
    }

    @Override
    public void stop() {
        log.info(" ====> zk client stop");
        client.close();
    }

    /**
     * 服务创建为持久节点，服务实例创建为临时节点。
     * @param service 全限定类名
     * @param instance ip+端口
     */
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            //创建服务的持久化节点，TODO 内容只是为了方便查看，无特殊作用
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            //创建实例节点，临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ====> register zookeeper,path="+instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provide".getBytes());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            //判断服务节点是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            //删除实例节点，临时节点
            String instancePath = servicePath + "/" + instance;
            log.info(" ====> unregister zookeeper,path="+instancePath);
            client.delete().quietly().forPath(instancePath);//删除失败不会报错
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta serviceMeta) {
        String servicePath = "/" + serviceMeta.toPath();
        try{
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info("fetchAll serviceName="+serviceMeta.getName()+",nodes="+nodes);
            return toInstanceMeta(nodes);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private List<InstanceMeta> toInstanceMeta(List<String> nodes){
        return nodes.stream().map(i -> {
            String[] datas = i.split("_");
            return InstanceMeta.http(datas[0], Integer.valueOf(datas[1]));
        }).collect(Collectors.toList());
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, ChangedListener changedListener) {
        String servicePath = "/" + serviceMeta.toPath();
        log.info("subscribe,service="+serviceMeta.toPath());
        try{
            final TreeCache cache = TreeCache.newBuilder(client, servicePath)
                    .setCacheData(true)
                    .setMaxDepth(2)
                    .build();
            cache.getListenable().addListener(
                    (curator, event) -> {
                        // 有任何节点变化，这段代码会执行。
                        log.info("zk subscribe event:"+event);
                        List<InstanceMeta> instanceMetas = fetchAll(serviceMeta);
                        changedListener.fire(new Event(instanceMetas));
                    }
            );
            cache.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void heartbeat() {

    }
}
