package com.laoyang.ware.service.mq;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.laoyang.common.enums.order.OrderStatusEnum;
import com.laoyang.common.to.mq.StockDetailTo;
import com.laoyang.common.to.mq.StockLockedTo;
import com.laoyang.common.to.order.OrderTo;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.order.OrderVo;
import com.laoyang.ware.entity.WareOrderTaskDetailEntity;
import com.laoyang.ware.entity.WareOrderTaskEntity;
import com.laoyang.ware.feign.OrderFeignService;
import com.laoyang.ware.service.WareInfoService;
import com.laoyang.ware.service.WareOrderTaskDetailService;
import com.laoyang.ware.service.WareOrderTaskService;
import com.laoyang.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-18 21:19
 * @Email yangyouyuhd@163.com
 */
@Component
@RabbitListener(queues = "stock.release.stock.queue")
public class MqListener {


    @Resource
    WareOrderTaskDetailService orderTaskDetailService;

    @Resource
    WareOrderTaskService wareOrderTaskService;



    @Resource
    WareSkuService wareSkuService;


    /**
     * 释放库存
     */
    @SneakyThrows
    @RabbitHandler
    public void listener(Message message, Channel channel, StockLockedTo stockLockedTo){
        boolean locked = wareSkuService.handleOrderStockRelease(stockLockedTo);
        if(locked){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }else {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     *   上游订单创建ok、发送延时消息被接收
     *   如果当前订单纪录最新状态为未支付或者已取消、则关单
     *   关单逻辑
     */
    @SneakyThrows
    @RabbitHandler
    public void listener(OrderTo orderTo, Message message, Channel channel){
        try {
            wareSkuService.handleOrderCloseRelease(orderTo);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
