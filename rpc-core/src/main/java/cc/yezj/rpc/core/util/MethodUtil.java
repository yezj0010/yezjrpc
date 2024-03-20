package cc.yezj.rpc.core.util;

import cc.yezj.rpc.core.annotation.YezjConsumer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<Field> findAnnotatedField(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        while(aClass != null){
            Field[] declaredFields = aClass.getDeclaredFields();
            for(Field ff : declaredFields){
                if(ff.isAnnotationPresent(annotationClass)){
                    result.add(ff);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }
}
