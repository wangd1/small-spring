package com.spring;

/**
 * @author wangd1
 * @date 2021/9/4
 */
public interface InitializingBean {

    void afterPropertiesSet() throws Exception;
}
