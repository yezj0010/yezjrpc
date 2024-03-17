package cc.yezj.rpc.core.api;

import java.util.List;

/**
 * 为了区分负载均衡（负载均衡就是从多个服务列表取出一个的行为）
 * 比如过滤出带有某个标签的服务列表，
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    Router Default = p -> p;
}
