package cc.yezj.rpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceMeta {

    private String name;//服务名称
    private String app;//不同的应用
    private String namespace;//不同的租户
    private String env;//环境

    private Map<String ,String> parameters = new HashMap<>();

    public String toPath(){
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }

    public String toMetas(){
        return JSON.toJSONString(this.getParameters());
    }
}
