package com.laoyang.order.controller.web;

import com.laoyang.order.config.pay.AlipayTemplate;
import com.laoyang.order.config.pay.PayVo;
import com.laoyang.order.service.OrderService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-07-19 16:54
 * @Email yangyouyuhd@163.com
 */
@Controller
public class PayController {

    @Resource
    AlipayTemplate alipayTemplate;

    @Resource
    OrderService orderService;


    /**
     *  支付宝支付处理接口、
     *
     * @param orderSn
     * @return
     */
    @SneakyThrows
    @ResponseBody
    @GetMapping(value = "/orderPay",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn ){
        PayVo payVo = orderService.getOrderPay(orderSn);
        String res = alipayTemplate.pay(payVo);
        return res;
    }


}
