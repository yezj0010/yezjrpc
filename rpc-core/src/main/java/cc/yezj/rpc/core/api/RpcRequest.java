package cc.yezj.rpc.core.api;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class RpcRequest implements Serializable {
    private String service;// 接口 cc.yezj.rpc.demo.api.UserService

    private String methodSign;// 方法 findById+方法个数+方法入参类型 MethodUtil.methodSign

    private Object[] args;// 参数

    private Map<String, String> properties;
}
