package com.example.zhizuo.api.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web页面控制器
 * 前后端一体化，不需要网络连接
 */
@Controller
public class WebController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 菜单/点餐页面
     */
    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }

    /**
     * 订单列表
     */
    @GetMapping("/orders")
    public String orders() {
        return "orders";
    }

    /**
     * 订单详情
     */
    @GetMapping("/order/detail")
    public String orderDetail() {
        return "order-detail";
    }

    /**
     * 我的/个人中心
     */
    @GetMapping("/mine")
    public String mine() {
        return "mine";
    }

    /**
     * 购物车/结算页面
     */
    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }
}

