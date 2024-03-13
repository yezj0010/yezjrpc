package cc.yezj.rpc.core.model.request;

import lombok.Data;

@Data
public class RpcRequest {
    private String service;// 接口 cc.yezj.rpc.demo.api.UserService

    private String methodSign;// 方法 findById+方法个数+方法入参类型 MethodUtil.methodSign

    private Object[] args;// 参数
}
