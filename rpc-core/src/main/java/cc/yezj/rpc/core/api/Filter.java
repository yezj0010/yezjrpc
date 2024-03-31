package cc.yezj.rpc.core.api;

public interface Filter {

    Object preFilter(RpcRequest request);

    Object postFilter(RpcRequest request, RpcResponse response, Object result);

//    Filter next();//也可以使用next，或者直接使用filter数组

    Filter Default = new Filter() {
        @Override
        public Object preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };

}
