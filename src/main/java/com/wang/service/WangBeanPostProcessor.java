package com.wang.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author wangd1
 * @date 2021/9/4
 */
@Component
public class WangBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if("userService".equals(beanName)){
            Object newProxyInstance = Proxy.newProxyInstance(WangBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑");
                    return method.invoke(bean,args);
                }
            });
            return newProxyInstance;
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if("userService".equals(beanName)){
            for (Field field : bean.getClass().getDeclaredFields()) {
                if(field.isAnnotationPresent(WangValue.class)){
                    field.setAccessible(true);
                    try {
                        field.set(bean,field.getAnnotation(WangValue.class).value());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bean;
    }
}
