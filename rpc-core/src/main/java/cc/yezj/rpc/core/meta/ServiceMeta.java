package cc.yezj.rpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceMeta {

    private String name;//服务名称
    private String app;//不同的应用
    private String namespace;//不同的租户
    private String env;//环境



    public String toPath(){
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
