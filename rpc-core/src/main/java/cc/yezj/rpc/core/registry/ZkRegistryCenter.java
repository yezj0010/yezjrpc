package cc.yezj.rpc.core.registry;

import cc.yezj.rpc.core.api.RegistryCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

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
    public void register(String service, String instance) {
        String servicePath = "/" + service;
        try {
            //创建服务的持久化节点，TODO 内容只是为了方便查看，无特殊作用
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            //创建实例节点，临时节点
            String instancePath = servicePath + "/" + instance;
            System.out.println(" ====> register zookeeper,path="+instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provide".getBytes());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String service, String instance) {
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
    public List<String> fetchAll(String serviceName) {
        return null;
    }

    @Override
    public void subscribe() {

    }
}