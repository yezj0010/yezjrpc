package cc.yezj.rpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class ProviderMeta {

    Method method;

    String methodSign;//methodSign和method放一起，避免反复反射

    Object serviceImpl;

}
