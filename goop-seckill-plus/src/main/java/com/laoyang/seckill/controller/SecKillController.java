package com.laoyang.seckill.controller;

import cn.hutool.core.lang.UUID;
import com.laoyang.common.util.R;
import com.laoyang.seckill.scheduled.server.inter.SecKillUpService;
import com.laoyang.seckill.xo.to.SecKillSkuRedisTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-08-02 15:56
 * @Email yangyouyuhd@163.com
 */
@Controller
public class SecKillController  {
    @Resource
    private SecKillUpService secKillUpService;



    /**
     * 获取当前正处于秒杀时间的秒杀场次集合
     * @return
     */
    @GetMapping(value = "/getCurrentSecKillSessionList")
    @ResponseBody
    public R getCurrentSecKillSkus() {

        //获取到当前可以参加秒杀商品的信息
        List<SecKillSkuRedisTo> vos = secKillUpService.getCurrentSecKillSessionList();

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

        SecKillSkuRedisTo to = secKillUpService.getSkuSecKilInfo(skuId);

        return R.ok().setData(to);
    }

    /**
     * 立即抢购秒杀商品
     *   抢购成功跳到订单确认页面、让用户快给钱
     *   抢购失败、则跳到异常页面
     * @param killId 秒杀场次和商品SkuId
     * @param num    秒杀数量
     * @return
     */
    @GetMapping(value = "/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("num") Integer num,
                          Model model) {
        try {
            String skuId = secKillUpService.kill(killId, num);
            return "redirect:http://order.goop.com/kill/toConfirm?skuId="+skuId+"&num="+num;
        } catch (Exception e) {
            /**
             * 抛出了各种稀奇古怪的异常、可以封装异常信息重定向到异常页面
             *   这次重定向到success页面是个幌子
             */
            e.printStackTrace();
        }
        model.addAttribute("orderSn", UUID.randomUUID().toString());
        return "success";
    }


    /**
     *  对秒杀请求进行校验
     *      如果满足秒杀条件则重定向到秒杀地址
     *   校验内容
     *      商品是否处于秒杀时间段
     *      随机码是否正确
     *   好处
     *      解耦
     *      隐藏真实秒杀地址、防刷
     * @return
     */
    @GetMapping(value = "/check/kill")
    public String secKillCheck(@RequestParam("killId") String killId,
                               @RequestParam("code") String code,
                               @RequestParam("num") Integer num,
                                RedirectAttributes redirectAttributes) {

        boolean flag = secKillUpService.killCheck(killId,code);
        redirectAttributes.addAttribute("killId",killId);
        redirectAttributes.addAttribute("num",num);
        return flag ? "redirect:http://seckill.goop.com/kill" : "非法请求";
    }

}
