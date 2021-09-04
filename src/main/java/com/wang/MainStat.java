package com.wang;

import com.spring.WangApplicationContext;
import com.wang.service.UserInterface;
import com.wang.service.UserService;

/**
 * @author wangd1
 */
public class MainStat {
    public static void main(String[] args) {
        // 扫描,创建bean
        WangApplicationContext ctx = new WangApplicationContext(AppConfig.class);
        UserInterface userService = (UserInterface) ctx.getBean("userService");
        userService.test();
    }
}
