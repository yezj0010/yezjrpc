package cc.yezj.rpc.core.registry;

import cc.yezj.rpc.core.api.ChangedListener;
import cc.yezj.rpc.core.api.RegistryCenter;
import cc.yezj.rpc.core.meta.InstanceMeta;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Override
    public void start() {
        //backoff每次去探测的时间间隔是上一次的2倍
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")//TODO 不能写死，待重构
                .namespace("yezjrpc")
                .retryPolicy(retryPolicy)
                .build();
        System.out.println(" ====> zk client start");
        client.start();
    }

    @Override
    public void stop() {
        System.out.println(" ====> zk client stop");
        client.close();
    }

    /**
     * 服务创建为持久节点，服务实例创建为临时节点。
     * @param service 全限定类名
     * @param instance ip+端口
     */
    @Override
    public void register(String service, InstanceMeta instance) {
        String servicePath = "/" + service;
        try {
            //创建服务的持久化节点，TODO 内容只是为了方便查看，无特殊作用
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            //创建实例节点，临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            System.out.println(" ====> register zookeeper,path="+instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provide".getBytes());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String service, InstanceMeta instance) {
        String servicePath = "/" + service;
        try {
            //判断服务节点是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            //删除实例节点，临时节点
            String instancePath = servicePath + "/" + instance;
            System.out.println(" ====> unregister zookeeper,path="+instancePath);
            client.delete().quietly().forPath(instancePath);//删除失败不会报错
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(String serviceName) {
        String servicePath = "/" + serviceName;
        try{
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println("fetchAll serviceName="+serviceName+",nodes="+nodes);
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
    public void subscribe(String serviceName, ChangedListener changedListener) {
        String servicePath = "/" + serviceName;
        System.out.println("subscribe,service="+serviceName);
        try{
            final TreeCache cache = TreeCache.newBuilder(client, servicePath)
                    .setCacheData(true)
                    .setMaxDepth(2)
                    .build();
            cache.getListenable().addListener(
                    (curator, event) -> {
                        // 有任何节点变化，这段代码会执行。
                        System.out.println("zk subscribe event:"+event);
                        List<InstanceMeta> instanceMetas = fetchAll(serviceName);
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
