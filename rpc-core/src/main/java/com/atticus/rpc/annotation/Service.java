package com.atticus.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示一个服务提供类，注解放在接口的实现类上。
 * name()的值代表需要注册的服务名，如果值为""，则表示将实现的服务接口全部注册
 */
// 表示注解的作用目标为接口、类、枚举类型
@Target(ElementType.TYPE)
// 表示在运行时可以动态获取注解信息
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

    String name() default "";
}
