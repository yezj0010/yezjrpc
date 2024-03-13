package cc.yezj.rpc.core.util;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class TypeUtils {

    /**
     * 解决类型转换问题
     */
    public static Object cast(Object origin, Class<?> type){
        if(origin == null){
            return null;
        }
        Class<?> aClass = origin.getClass();
        if(type.isAssignableFrom(aClass)){
            return origin;
        }

        //处理map
        if(origin instanceof Map){
            String jsonString = JSONObject.toJSONString(origin);
            return JSONObject.parseObject(jsonString, type);
        }

        if(type.isArray()){
            if(origin instanceof List list){
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> coType = type.getComponentType();
            Object resultArray = Array.newInstance(coType, length);
            for(int i=0;i<length; i++){
                Array.set(resultArray, i, Array.get(origin, i));
            }
            return resultArray;
        }

        //处理数组

        if(type.equals(Long.class) || type.equals(Long.TYPE)){
            return Long.valueOf(origin.toString());
        }
        return origin;
    }
}
