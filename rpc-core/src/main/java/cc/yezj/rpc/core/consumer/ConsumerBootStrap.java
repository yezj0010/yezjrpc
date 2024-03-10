package cc.yezj.rpc.core.consumer;

import cc.yezj.rpc.core.annotation.YezjConsumer;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ConsumerBootStrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanDefinitionNames) {
            Object bean = applicationContext.getBean(name);
            List<Field> annotatedField = findAnnotatedField(bean.getClass());
            annotatedField.stream().forEach(i -> {
                try{
                    Class<?> type = i.getType();
                    String classFullName = type.getCanonicalName();//拿到全限定名称
                    Object o = stub.get(classFullName);//获取代理类，判断是否存在
                    if (o == null) {
                        o = createConsumer(type);
                    }
                    i.setAccessible(true);
                    i.set(bean, o);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    private Object createConsumer(Class<?> serviceClass) {
        return Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class[]{serviceClass}, new ConsumerInvocationHandler(serviceClass));
    }

    private List<Field> findAnnotatedField(Class<?> aClass) {
        List<Field> result = new ArrayList<>();
        while(aClass != null){
            Field[] declaredFields = aClass.getDeclaredFields();
            for(Field ff : declaredFields){
                if(ff.isAnnotationPresent(YezjConsumer.class)){
                    result.add(ff);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }
}
