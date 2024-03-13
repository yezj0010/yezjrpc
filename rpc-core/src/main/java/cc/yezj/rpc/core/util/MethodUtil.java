package cc.yezj.rpc.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodUtil {

    public static String methodSign(Method method){
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                i -> sb.append("_").append(i.getCanonicalName())
        );
        return sb.toString();
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtil.class.getMethods()).forEach(i -> System.out.println(methodSign(i)));
    }

    public static boolean checkLocalMethod(Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }
}
