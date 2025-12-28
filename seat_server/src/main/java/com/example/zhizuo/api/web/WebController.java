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
    
    /**
     * 后台管理页面 - 直接返回静态HTML文件
     */
    @GetMapping("/admin.html")
    public String admin() {
        return "forward:/static/admin.html";
    }
    
    /**
     * 商品管理页面 - 直接返回静态HTML文件
     */
    @GetMapping("/product_manage.html")
    public String productManage() {
        return "forward:/static/product_manage.html";
    }
    
    /**
     * AI监控大屏页面 - 直接返回静态HTML文件
     */
    @GetMapping("/monitor.html")
    public String monitor() {
        return "forward:/static/monitor.html";
    }
    
    /**
     * 反馈管理页面 - 直接返回静态HTML文件
     */
    @GetMapping("/feedback_manage.html")
    public String feedbackManage() {
        return "forward:/static/feedback_manage.html";
    }
    
    /**
     * 门店订单管理页面 - 直接返回静态HTML文件
     */
    @GetMapping("/store_order_manage.html")
    public String storeOrderManage() {
        return "forward:/static/store_order_manage.html";
    }
}

