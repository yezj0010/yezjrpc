package cc.yezj.rpc.core.meta;

import lombok.Data;

@Data
public class ServiceMeta {

    private String app;//不同的应用
    private String namespace;//不同的租户
    private String env;//环境
    private String name;//服务名称
}
