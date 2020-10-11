package com.laoyang.cart.controller;

import com.laoyang.cart.server.CartService;
import com.laoyang.cart.xo.vo.CartItemVo;
import com.laoyang.cart.xo.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author yyy
 * @Date 2020-07-10 17:03
 * @Email yangyouyuhd@163.com
 */
@Controller
public class CartController {

    @Resource
    CartService cartService;

    /**
     * 去购物车页面的请求
     *      浏览器有一个cookie:user-key 标识用户的身份，一个月过期
     *      如果第一次使用jd的购物车功能，都会给一个临时的用户身份:
     *      浏览器以后保存，每次访问都会带上这个cookie；
     * 已登录：redis有用户的session、也有user-key
     * 没登录：按照cookie里面带来user-key来做
     * 第一次，如果没有临时用户，自动创建一个临时用户/cookie/user-key
     *
     * @return
     */
    @GetMapping(value = "/cart.html")
    public String cartListPage(Model model) {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }


    /**
     * http://cart.goop.com/addCartItem?skuId="+skuId+"&num="+num;
     * 添加商品到购物车
     *      attributes.addFlashAttribute():将数据放在session中，重定向后取出即删
     *      attributes.addAttribute():重定向后自动将数据拼在url后面
     * 防止重复购物车提交：
     *      1、购物车更新和查询分离、
     *          购物车更新后重定义查询一次、
     *          此后刷新查询对购物车无影响
     */
    @GetMapping("/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId,
                              @RequestParam("num") Integer num,
                              RedirectAttributes redirect) {
        cartService.addCartItem(skuId, num);
        redirect.addAttribute("skuId", skuId);
        return "redirect:http://cart.goop.com/addCartItemSuccessPage.html";
    }

    /**
     * 获取对应的购物项、根据skuId
     * 该请求仅供添加购物项成功后重定向查询使用
     * @return
     */
    @GetMapping("/addCartItemSuccessPage.html")
    public String addCartItemSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }

    /**
     * 更新购物项选中状态
     * @return
     */
    @GetMapping(value = "/checkItem")
    public String checkItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "checked") Integer checked) {
        cartService.checkItem(skuId, checked);
        return "redirect:http://cart.goop.com/cart.html";
    }

    /**
     * 更新购物项数量
     * @return
     */
    @GetMapping(value = "/countItem")
    public String countItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "num") Integer num) {
        cartService.updateItemCount(skuId, num);
        return "redirect:http://cart.goop.com/cart.html";
    }

    /**
     * 删除购物项
     * @return
     */
    @GetMapping(value = "/deleteItem")
    public String deleteItem(@RequestParam("skuId") Integer skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.goop.com/cart.html";
    }


    /**
     * 获取当前用户已选中待支付的购物项集合
     * @return
     */
    @GetMapping(value = "/checkedItems")
    @ResponseBody
    public List<CartItemVo> getCurrentCartItems() {

        List<CartItemVo> cartItemVoList = cartService.getCheckedItems();
        return cartItemVoList;
    }
}
