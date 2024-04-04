package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.api.Filter;
import cc.yezj.rpc.core.api.LoadBalancer;
import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.api.RpcContext;
import cc.yezj.rpc.core.api.RpcException;
import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.governance.SlidingTimeWindow;
import cc.yezj.rpc.core.meta.InstanceMeta;
import cc.yezj.rpc.core.util.MethodUtil;
import cc.yezj.rpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsumerInvocationHandler implements InvocationHandler {
    private final Class<?> service;

    private final RpcContext rpcContext;

    private final List<InstanceMeta> providers;

    final private List<InstanceMeta> isolatedProviders = new ArrayList<>();

    Map<String, SlidingTimeWindow> windows = new HashMap<>();//单位时间内故障数超过多少，算故障。进行隔离

    HttpInvoker httpInvoker;

    ScheduledExecutorService executorService;

    final private List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    public ConsumerInvocationHandler(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers){
        this.service = service;
        this.rpcContext = rpcContext;
        this.providers = providers;
        this.httpInvoker = new OKHttpInvoker(Integer.parseInt(rpcContext.getParameters().getOrDefault("app.timeout", "1000")));
        this.executorService = Executors.newScheduledThreadPool(1);
        this.executorService.scheduleWithFixedDelay(this::halfOpen, 10, 10, TimeUnit.MINUTES);
    }

    AtomicInteger a = new AtomicInteger();
    private void halfOpen() {
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
        log.debug("halfOpenProviders = "+halfOpenProviders +",count = "+a.getAndIncrement());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtil.checkLocalMethod(method)) {
            return null;
        }

        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);
        log.debug("request = " + request);

        int retry = Integer.parseInt(rpcContext.getParameters().getOrDefault("app.retries", "2"));
        while(retry > 0) {
            try {
                log.debug("ConsumerInvocationHandler.invoke, retry="+retry);
                for (Filter filter : rpcContext.getFilters()) {
                    Object preResult = filter.preFilter(request);
                    if (preResult != null) {
                        log.debug(filter.getClass().getName() + ", preFilterResult:" + preResult);
                        return preResult;
                    }
                }
                InstanceMeta instanceMeta = null;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<String> nodes = rpcContext.getRouter().route(providers);
                        instanceMeta = (InstanceMeta) rpcContext.getLoadBalancer().choice(nodes);
                        log.debug("loadBalancer.choice => " + instanceMeta);
                    } else {
                        instanceMeta = halfOpenProviders.remove(0);
                        log.debug("check alive instance="+instanceMeta);
                    }
                }

                RpcResponse<?> response;
                Object result;
                String url = instanceMeta.getUrl();
                try {
                    response = httpInvoker.post(request, url);
                    log.debug("response = " + response);
                    result = castReturnResult(method, response);
                }catch (Exception e){
                    // 故障的规则统计和隔离
                    // 针对服务实例，计算单位时间内异常次数
                    SlidingTimeWindow slidingTimeWindow = windows.computeIfAbsent(url, k -> new SlidingTimeWindow());
                    //异常一次，就记录一次，传当前时间戳
                    slidingTimeWindow.record(System.currentTimeMillis());
                    log.debug("instance {} in window with {}", url, slidingTimeWindow.getSum());
                    //发生了10次，就做故障隔离
                    if(slidingTimeWindow.getSum() >= 10){//TODO 配置
                        isolate(instanceMeta);
                    }
                    throw e;
                }

                //隔离恢复
                synchronized (providers) {//加同步快，是为了防止并发处理的报错
                    if (!providers.contains(instanceMeta)) {
                        isolatedProviders.remove(instanceMeta);
                        providers.add(instanceMeta);
                        log.debug("instance {} is recovered, isolatedProviders={}, providers={}", instanceMeta, isolatedProviders, providers);
                    }
                }

                //调用后过滤
                for (Filter filter : rpcContext.getFilters()) {
                    Object filterResult = filter.postFilter(request, response, result);
                    if (filterResult != null) {
                        return filterResult;//TODO 这里逻辑好像有问题，一旦一个过滤器生效了，后面的过滤器就没意义了。
                    }
                }
                return result;
            } catch (SocketTimeoutException sto) {
                retry--;
                log.error("timeout", sto);
            } catch (Exception e) {
                log.error("error", e);
                break;
            }
        }
        return null;
    }

    private void isolate(InstanceMeta instanceMeta) {
        log.debug("==> isolate instance:" + instanceMeta);
        providers.remove(instanceMeta);
        log.debug("==> new providers list:" + providers);
        isolatedProviders.add(instanceMeta);
        log.debug("==> new isolated providers list:" + isolatedProviders);
    }


    @Nullable
    private static Object castReturnResult(Method method, RpcResponse<?> response) {
        if(response != null && response.isSuccess() && response.getData() != null){
            return TypeUtils.castMethodResult(method, response.getData());
        } else {
            if(response != null && response.getException() != null){
                if(response.getException() instanceof RpcException exception){
                    throw exception;
                }
                throw new RpcException(response.getException(), RpcException.INTER_ERROR_EX);
            }
            return null;
        }
    }


}
