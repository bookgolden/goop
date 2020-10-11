package com.laoyang.order.service;

import com.laoyang.common.to.mq.SeckillOrderTo;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.vo.order.OrderVo;
import com.laoyang.order.config.pay.PayAsyncVo;
import com.laoyang.order.config.pay.PayVo;
import com.laoyang.order.entity.OrderEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.order.xo.vo.OrderConfirmVo;
import com.laoyang.order.xo.vo.OrderSubmitResultVo;
import com.laoyang.order.xo.vo.OrderSubmitVo;

import java.util.Map;

/**
 * 订单
 *
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 封装订单确认页数据
     * @return
     */
    OrderConfirmVo confirmOrder();

    /**
     * 订单提交、处理业务
     * @param submitVo
     * @return
     */
    OrderSubmitResultVo submitOrder(OrderSubmitVo submitVo);

    /**
     * 根据订单号 获取订单info
     * @param orderSn
     * @return
     */
    OrderVo getOneByOrderSn(String orderSn);

    /**
     * 试图关闭订单、
     *   如果订单状态任然是新建状态
     * @param orderEntity
     */
    void closeOrder(OrderEntity orderEntity);

    /**
     *  支付宝支付vo封装
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    /**
     *  获取订单列表
     * @param params
     * @return
     */
    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * 创建秒杀单
     * @param orderTo
     */
    void createSeckillOrder(SeckillOrderTo orderTo);

    /**
     * 处理支付宝支付结果
     * @param asyncVo
     * @return
     */
    String handlePayResult(PayAsyncVo asyncVo);

}

