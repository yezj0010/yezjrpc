package cc.yezj.rpc.core.api;

public class RpcException extends RuntimeException{

    private String errorCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    // T => 技术类异常
    // B => 业务类异常
    // U => unknown
    public static final String SOCKET_TIME_OUT_EX = "T001" + "-" +"http_invoke_timeout";
    public static final String NO_SUCH_METHOD_EX = "T002" + "-" +"http_invoke_timeout";

    public static final String INTER_ERROR_EX = "U001" + "-" +"internal_error";

}
