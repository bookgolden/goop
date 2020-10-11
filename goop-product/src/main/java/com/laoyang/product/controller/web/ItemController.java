package com.laoyang.product.controller.web;

import com.laoyang.product.server.inter.SkuInfoService;
import com.laoyang.product.vo.web.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-06-30 11:25
 * @Email yangyouyuhd@163.com
 * @Note
 */
@Controller
public class ItemController {

    @Resource
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemVo skuItem = skuInfoService.getSkuItemById(skuId);
        model.addAttribute("item",skuItem);
        return "item";
    }
}
