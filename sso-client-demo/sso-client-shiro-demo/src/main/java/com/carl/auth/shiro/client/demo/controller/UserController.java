/*
 * 版权所有.(c)2008-2017. 卡尔科技工作室
 */


package com.carl.auth.shiro.client.demo.controller;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jasig.cas.client.jaas.ServiceAndTicketCallbackHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Carl
 * @date 2017/9/16
 * @since 1.0.0
 */
@RestController
@RequestMapping("user")
public class UserController {


    @RequestMapping(value = "test1",method = RequestMethod.GET)
    @RequiresRoles("zms")
    public Object user(String id) {
        return "users page:" + id;
    }

    @RequestMapping(value="test",method = RequestMethod.GET)
    @RequiresRoles("1")
    public Object detail(HttpServletRequest request) {
        //用户详细信息

        return null;
    }
}
