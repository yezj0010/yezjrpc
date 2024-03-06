package cc.yezj.rpc.core.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {
    private boolean success;
    private int code;
    private T data;
}
