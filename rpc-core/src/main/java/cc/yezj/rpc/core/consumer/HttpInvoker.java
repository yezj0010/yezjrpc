package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.api.RpcRequest;
import cc.yezj.rpc.core.api.RpcResponse;

public interface HttpInvoker {

    RpcResponse<Object> post(RpcRequest rpcRequest, String url);
}
