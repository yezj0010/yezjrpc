package cc.yezj.rpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 服务实例元数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {

    private String scheme;
    private String host;
    private Integer port;
    private String context;//路径的上下文 就是web服务路径

    private boolean status;//服务端状态， online or offline
    private Map<String, String> parameters; //可以包括IDC机房所在地，等属性

    InstanceMeta(String scheme, String host, Integer port, String context){
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public static InstanceMeta http(String host, Integer port){
        return new InstanceMeta("http", host, port, "");
    }

    public String toPath(){
        return host + "_" + port;
    }

    public String getUrl(){
        return scheme+"://"+host+":"+port+"/";
    }
}
