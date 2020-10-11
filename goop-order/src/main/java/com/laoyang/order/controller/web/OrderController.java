package com.laoyang.order.controller.web;

import com.laoyang.common.util.PageUtils;
import com.laoyang.order.config.excep.OrderSubmitException;
import com.laoyang.order.feign.MemberFeignService;
import com.laoyang.order.service.OrderService;
import com.laoyang.order.xo.vo.OrderConfirmVo;
import com.laoyang.order.xo.vo.OrderSubmitResultVo;
import com.laoyang.order.xo.vo.OrderSubmitVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yyy
 * @Date 2020-07-14 19:33
 * @Email yangyouyuhd@163.com
 */
@Controller
public class OrderController {

    @Resource
    OrderService orderService;


    /**
     *  选好购物项、跳到订单确认页面
     * @param model
     * @return
     */
    @GetMapping("/toConfirm")
    public String toConfirm(Model model){
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirmOrderData",confirmVo);
        return "confirm";
    }

    /**
     *  用户提交订单、处理请求
     * @param submitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo,Model model){
        try {
            OrderSubmitResultVo resultVo = orderService.submitOrder(submitVo);
            model.addAttribute("res",resultVo);
            return "pay";
        }catch (OrderSubmitException e){
            System.out.println("失败的状态码："+e.code);
            System.out.println("失败的原因："+e.msg);
            return "confirm";
        }
    }

    @GetMapping(value = "/order/order/memberOrder.html")
    public String memberOrderPage(
            @RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum,
            Model model) {

        //获取到支付宝给我们转来的所有请求数据
        //request,验证签名
        //查出当前登录用户的所有订单列表数据
        Map<String,Object> params = new HashMap<>();
        params.put("page",pageNum.toString());

        PageUtils page = orderService.queryPageWithItem(params);
//        return R.ok().put("page", page);

        model.addAttribute("orders",page);
        return "orderList";
    }
}
