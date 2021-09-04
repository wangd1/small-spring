package com.wang.service;

import com.spring.*;

/**
 * @author wangd1
 */
@Component("userService")
// @Scope("prototype")
public class UserService implements UserInterface, BeanNameAware {

    @Autowired
    private OrderService orderService;

    @WangValue("wangdi")
    private String name;
    private String beanName;

    @Override
    public void test(){
        System.out.println(beanName);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
