package com.laoyang.seckill.controller;

import com.laoyang.common.util.R;
import com.laoyang.seckill.server.inter.SecKillService;
import com.laoyang.seckill.xo.to.SecKillSkuRedisTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-24 13:45
 * @Email yangyouyuhd@163.com
 */
@Controller
public class SecKillController {
    @Resource
    private SecKillService seckillService;

    /**
     * 当前时间可以参与秒杀的商品信息
     *
     * @return
     */
    @GetMapping(value = "/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSecKillSkus() {

        //获取到当前可以参加秒杀商品的信息
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSecKillSkus();

        return R.ok().setData(vos);
    }

    /**
     * 根据skuId查询商品是否参加秒杀活动
     *
     * @param skuId
     * @return
     */
    @GetMapping(value = "/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckilInfo(@PathVariable("skuId") Long skuId) {

        SecKillSkuRedisTo to = seckillService.getSkuSeckilInfo(skuId);

        return R.ok().setData(to);
    }

    /**
     * 立即抢购秒杀商品
     *
     * @param killId 秒杀场次和商品SkuId
     * @param code   校验码
     * @param num    秒杀数量
     * @return
     */
    @GetMapping(value = "/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("code") String code,
                          @RequestParam("num") Integer num,
                          Model model) {

        String orderSn = "null";
        try {
            //1、判断是否登录
             orderSn = seckillService.kill(killId, code, num);
            model.addAttribute("orderSn", orderSn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }
}
