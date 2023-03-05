package com.xu.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 * 功能：防止未登陆的情况下用户直接通过修改浏览器访问路径的方式调用某些方法。
 * 自定义注解需要用java的注解配置
 * @target:定义注解的作用对象
 * @retention：定义注解的生效时间
 */
@Target(ElementType.METHOD)//作用于方法
@Retention(RetentionPolicy.RUNTIME)//运行时生效
public @interface LoginRequired {
}
