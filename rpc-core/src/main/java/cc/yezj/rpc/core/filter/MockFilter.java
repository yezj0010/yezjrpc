package cc.yezj.rpc.core.filter;

import cc.yezj.rpc.core.api.Filter;
import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;
import cc.yezj.rpc.core.util.MethodUtil;
import cc.yezj.rpc.core.util.MockUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * TODO mock逻辑可以这样写，也可以在创建客户端代理的时候，就只创建mock版本的代理
 * 也就是在cc.yezj.rpc.core.consumer.ConsumerBootStrap#createConsumer(java.lang.Class, cc.yezj.rpc.core.api.RpcContext, java.util.List)
 * 这个方法创建代理的时候，就根据上下文中带的参数，来判断是否返回创建mock数据的代理，而不是调用远程的代理。
 */
@Slf4j
public class MockFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        Class service = null;
        try {
            service = Class.forName(request.getService());
        }catch (Exception e){
            log.error("preFilter error", e);
        }
        Method method = findMethod(service, request.getMethodSign());

        return MockUtils.mock(method.getReturnType());
    }

    Method findMethod(Class service, String methodSign){
        if(service == null){
            return null;
        }
        Method[] methods = service.getMethods();
        for(Method method : methods){
            if(MethodUtil.checkLocalMethod(method)){
                continue;
            }
            String tempSign = MethodUtil.methodSign(method);
            if(tempSign.equals(methodSign)){
                return method;
            }
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}
