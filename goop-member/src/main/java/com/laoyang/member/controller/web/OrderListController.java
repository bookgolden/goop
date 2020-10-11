package com.laoyang.member.controller.web;

import com.alibaba.fastjson.JSON;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.order.OrderVo;
import com.laoyang.member.feign.OrderFeignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yyy
 * @Date 2020-07-19 20:33
 * @Email yangyouyuhd@163.com
 */
@Controller
public class OrderListController {

    @Resource
    private OrderFeignService orderFeignService;

    @GetMapping(value = "/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum,
                                  Model model, HttpServletRequest request) {

        //获取到支付宝给我们转来的所有请求数据
        //request,验证签名
        //查出当前登录用户的所有订单列表数据
        Map<String,Object> page = new HashMap<>();
        page.put("page",pageNum.toString());

        //远程查询订单服务订单数据
        R<OrderVo> order =orderFeignService.listWithItem(page);
//        System.out.println(JSON.toJSONString(order));

        model.addAttribute("orders",order);
        return "orderList";
    }

}
