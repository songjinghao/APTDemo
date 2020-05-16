package com.songjinghao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by songjinghao on 2019/8/26.
 */
@Target(ElementType.TYPE) // 注解作用在类之上
@Retention(RetentionPolicy.CLASS) // 在源码和class文件中都存在，编译期注解方式
public @interface ARouter {

    String path();
}
