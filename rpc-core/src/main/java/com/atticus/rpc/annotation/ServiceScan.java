package com.atticus.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识服务的扫描的包的范围。
 * value()的值代表扫描范围的根包，如果值为""，则表示扫描启动类所在的包及其子包
 */
// 表示注解的作用目标为接口、类、枚举类型
@Target(ElementType.TYPE)
// 表示在运行时可以动态获取注解信息
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceScan {

    String value() default "";
}
