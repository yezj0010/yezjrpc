package cc.yezj.rpc.core.model.request;

import lombok.Data;

@Data
public class RpcRequest {
    private String service;// 接口 cc.yezj.rpc.demo.api.UserService

    private String method;// 方法 findById

    private Object[] args;// 参数
}
