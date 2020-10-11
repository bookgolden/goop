package com.laoyang.order.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.laoyang.common.to.mq.SeckillOrderTo;
import com.laoyang.common.to.order.OrderTo;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.member.MemberAddressVo;
import com.laoyang.common.vo.order.OrderItemVo;
import com.laoyang.common.vo.order.OrderVo;
import com.laoyang.common.vo.product.SpuInfoVo;
import com.laoyang.common.vo.user.MemberSessionTo;
import com.laoyang.common.vo.ware.FareVo;
import com.laoyang.common.vo.ware.SkuStockVo;
import com.laoyang.common.vo.ware.WareSkuLockVo;
import com.laoyang.order.config.OrderConstant;
import com.laoyang.common.enums.order.OrderStatusEnum;
import com.laoyang.order.config.excep.OrderSubmitException;
import com.laoyang.order.config.interceptor.LoginInterceptor;
import com.laoyang.order.config.pay.PayAsyncVo;
import com.laoyang.order.config.pay.PayVo;
import com.laoyang.order.dao.OrderDao;
import com.laoyang.order.entity.OrderEntity;
import com.laoyang.order.entity.OrderItemEntity;
import com.laoyang.order.entity.PaymentInfoEntity;
import com.laoyang.order.feign.CartFeignService;
import com.laoyang.order.feign.MemberFeignService;
import com.laoyang.order.feign.ProductFeignService;
import com.laoyang.order.feign.WmsFeignService;
import com.laoyang.order.service.OrderItemService;
import com.laoyang.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.order.service.PaymentInfoService;
import com.laoyang.order.service.RefundInfoService;
import com.laoyang.order.xo.to.OrderCreateTo;
import com.laoyang.order.xo.vo.*;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    MemberFeignService memberFeignService;

    @Resource
    CartFeignService cartFeignService;

    @Resource
    ThreadPoolExecutor executor;

    @Resource
    WmsFeignService wmsFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    OrderItemService orderItemService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    PaymentInfoService paymentInfoService;

    @Resource
    RefundInfoService refundInfoService;

    @SneakyThrows
    @Override
    public OrderConfirmVo confirmOrder() {
        MemberSessionTo memberSessionTo = LoginInterceptor.orderThread.get();
        OrderConfirmVo res = new OrderConfirmVo();
        //获取主线程的请求数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 主线程的请求数据同步到副线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1、获取用户收货列表
            List<MemberAddressVo> address = memberFeignService.listReceiveAddress(memberSessionTo.getId());
            res.setMemberAddressVos(address);
        }, executor);


        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            // 主线程的请求数据同步到副线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2、获取用户选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentCartItems();
            res.setItems(items);
        }, executor).thenRunAsync(() -> {
            /**
             *  1、获取所有待支付购物项的skuId
             *  2、调用库存服务查询各自的现有库存
             *  3、从结果集中获取每个skuId对应的当前库存数据
             *  4、映射为Map<skuId,stockTotal> 结构、封装到res结果
             */
            // TODO 已知每个购物项的当前库存和订购数量、需要进行库存is充足和锁定库存的操作
            List<Long> skuIds = res.getItems().stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R<List<SkuStockVo>> skuStock = wmsFeignService.selectSkuStock(skuIds);
            List<SkuStockVo> skuStockVoList = skuStock.getDate(new TypeReference<List<SkuStockVo>>() {
            });
            Map<Long, Long> skuStocks = Collections.EMPTY_MAP;
            if (skuStockVoList != null && !skuStockVoList.isEmpty()) {
                skuStocks = skuStockVoList.stream()
                        .collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getStockTotal));
            }
            res.setStocks(skuStocks);
        }, executor);

        // 3、积分叠加
        res.setIntegration(memberSessionTo.getIntegration());

        //TODO 4、防重令牌
        // 为用户设置一个token，三十分钟过期时间（存在redis）
        String token = UUID.randomUUID().toString().replace("-", "+");
        stringRedisTemplate.opsForValue()
                .set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberSessionTo.getId(), token, 30, TimeUnit.MINUTES);
        res.setOrderToken(token);

        // 等待
        CompletableFuture.allOf(addressFuture, itemsFuture).get();
        return res.postInit();
    }

    /**
     * 提交订单、处理请求
     * 1、OrderSubmitResultVo  最终处理结果的封装
     * 2、MemberSessionTo  存于redis的用户数据
     *
     * @param submitVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public OrderSubmitResultVo submitOrder(OrderSubmitVo submitVo) {
        OrderSubmitResultVo resultVo = new OrderSubmitResultVo();
        // 获取当前用户数据
        MemberSessionTo memberSessionTo = LoginInterceptor.orderThread.get();
        Long result = checkOrderToken(submitVo.getOrderToken(), memberSessionTo.getId());
        if (result != 0L) {
            /**
             * 令牌验证通过、处理业务
             *   1、创建订单
             *   2、验价、
             *   3、锁库存
             */
            OrderCreateTo orderCreateTo = createOrder(submitVo, memberSessionTo.getId());

            BigDecimal payAmount = orderCreateTo.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) > 3) {
                // 验价失败

                // 价格差异过大、应提醒用户重新下单
                resultVo.setCode(20030);
                throw OrderSubmitException.PRICE_CHECK_ERR;
            } else {
                // 验价通过、

                //1、保存订单数据
                saveOrder(orderCreateTo);
                /**
                 * 2、远程锁库存
                 *  2.1、封装远程锁库存需要的订单号和订单项数据 WareSkuLockVo
                 *  2.2、将封装好的 WareSkuLockVo 发送至仓储远程 锁库存
                 *  2.3、锁库存结果处理、
                 */
                // 2.1
                WareSkuLockVo lockVo = orderLockStock(orderCreateTo);
                // 2.2
                R lockStockRes = wmsFeignService.orderLockStock(lockVo);
                // 2.3
                if (lockStockRes.getCode() != 200) {
                    // 库存锁定失败
                    throw OrderSubmitException.LOCK_STOCK_ERR;
                } else {
                    /**
                     *  锁定库存成功、
                     *  将订单数据发往死信队列、30m后在检查订单状态是否需要关单
                     *  封装订单数据、返回
                     */
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderCreateTo.getOrder());
                    resultVo.setOrder(orderCreateTo.getOrder());
                    //TODO 删除购物车内已选中的购物项( 因为已选中的购物项被购买了、)
                }
            }
        } else {
            // token校验失败
            resultVo.setCode(20020);
            throw OrderSubmitException.TOKEN_CHECK_ERR;
        }
        return resultVo;
    }

    @Override
    public OrderVo getOneByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(orderEntity, orderVo);
        return orderVo;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 拿到订单号、查询最新的订单纪录
        OrderEntity orderInfo = this.getOne(new QueryWrapper<OrderEntity>().
                eq("order_sn", orderEntity.getOrderSn()));

        /**
         * 只有订单还是新建待支付状态的、才需要关单 X
         * 如果用户30m内完成支付、订单将变为待发货、状态==1
         * 如果用户30m内取消订单、订单将变为已关闭、状态==4
         * 只要此时订单状态 == 0、才需要关单
         */
        if (orderInfo.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            //代付款状态进行关单
            OrderEntity orderDao = new OrderEntity();
            orderDao.setId(orderInfo.getId());
            orderDao.setStatus(OrderStatusEnum.CANCEL.getCode());
            this.updateById(orderDao);

            /**
             *  订单关闭成功后、
             *  主动通知 仓储服务 解锁库存、
             */
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderInfo, orderTo);
            try {
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                // 消息发送失败处理
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderVo orderVo = this.getOneByOrderSn(orderSn);
        payVo.setOut_trade_no(orderSn);

        BigDecimal scale = orderVo.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(scale.toString());

        payVo.setSubject("主题" + orderVo.getOrderSn());
        payVo.setBody("body");
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberSessionTo memberSessionTo = LoginInterceptor.orderThread.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", memberSessionTo.getId()).orderByDesc("create_time")
        );

        //遍历所有订单集合
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            //根据订单号查询订单项里的数据
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntityList(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);

//        // 将数据载体 由 entity -> vo
//        List<OrderVo> res = page.getRecords().stream().map(order -> {
//            //根据订单号查询订单项里的数据
//            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
//                    .eq("order_sn", order.getOrderSn()));
//
//            List<OrderItemVo> orderItemVos = orderItemEntities.stream().map(orderItemEntity -> {
//                OrderItemVo orderItemVo = new OrderItemVo();
//                BeanUtils.copyProperties(orderItemEntity, orderItemVo);
//                return orderItemVo;
//            }).collect(Collectors.toList());
//
//            OrderVo orderVo = new OrderVo();
//            BeanUtils.copyProperties(order, orderVo);
//            orderVo.setOrderItemEntityList(orderItemVos);
//            return orderVo;
//        }).collect(Collectors.toList());
//
//        page.setRecords(res);
//        return new PageUtils(page);
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {

        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = orderTo.getSeckillPrice().multiply(BigDecimal.valueOf(orderTo.getNum()));
        orderEntity.setPayAmount(totalPrice);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //保存订单
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(orderTo.getOrderSn());
        orderItem.setRealAmount(totalPrice);

        orderItem.setSkuQuantity(orderTo.getNum());

        //保存商品的spu信息
        R<SpuInfoVo> spuInfo = productFeignService.getSpuInfoBySkuId(orderTo.getSkuId());
        SpuInfoVo spuInfoData = spuInfo.getDate(new TypeReference<SpuInfoVo>() {
        });
        orderItem.setSpuId(spuInfoData.getId());
        orderItem.setSpuName(spuInfoData.getSpuName());
        orderItem.setSpuBrand(spuInfoData.getBrandName());
        orderItem.setCategoryId(spuInfoData.getCatalogId());

        //保存订单项数据
        orderItemService.save(orderItem);
    }

    @Override
    @Transactional
    public String handlePayResult(PayAsyncVo asyncVo) {
        //保存交易流水信息
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(asyncVo.getOut_trade_no());
        paymentInfo.setAlipayTradeNo(asyncVo.getTrade_no());
        paymentInfo.setTotalAmount(new BigDecimal(asyncVo.getBuyer_pay_amount()));
        paymentInfo.setSubject(asyncVo.getBody());
        paymentInfo.setPaymentStatus(asyncVo.getTrade_status());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(asyncVo.getNotify_time());
        //添加到数据库中
        this.paymentInfoService.save(paymentInfo);

        //修改订单状态
        //获取当前状态
        String tradeStatus = asyncVo.getTrade_status();

        if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
            //支付成功状态
            String orderSn = asyncVo.getOut_trade_no(); //获取订单号
            this.updateOrderStatus(orderSn,OrderStatusEnum.PAYED.getCode(),OrderConstant.ALIPAY);
        }

        return "success";
    }




    /**
     * 封装订单项、发完仓储服务锁定库存
     *
     * @param orderCreateTo
     * @return
     */
    private WareSkuLockVo orderLockStock(OrderCreateTo orderCreateTo) {
        WareSkuLockVo lockVo = new WareSkuLockVo();
        // 封装订单号
        lockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
        // 封装要所库存的订单项集合
        List<OrderItemVo> orderItemVos = orderCreateTo.getOrderItems().stream().map((item) -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(item.getSkuId());
            orderItemVo.setCount(item.getSkuQuantity());
            orderItemVo.setTitle(item.getSkuName());
            return orderItemVo;
        }).collect(Collectors.toList());
        lockVo.setLocks(orderItemVos);
        return lockVo;
    }

    /**
     * 保存订单所有数据
     *
     * @param orderCreateTo
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {

        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        //保存订单
        this.baseMapper.insert(order);

        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);
    }


    /**
     * 创建订单 by 提交的订单数据
     *
     * @param submitVo
     * @return
     */
    private OrderCreateTo createOrder(OrderSubmitVo submitVo, Long memberId) {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        // 创建订单实体封装
        OrderEntity orderEntity = createOrderEntity(submitVo.getAddrId());
        // 1、分布式全局唯一订单号创建
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberId);
        orderCreateTo.setOrder(orderEntity);

        // 创建订单项集合封装
        List<OrderItemEntity> orderItemEntityList = createOrderItems(orderSn);
        orderCreateTo.setOrderItems(orderItemEntityList);

        // 计算各类价
        computePrice(orderEntity, orderItemEntityList);
        return orderCreateTo;
    }

    /**
     * 计算各类价格
     *
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 每笔订单关联了多个购物项
     * 创建 OrderCreateTo 需要的 OrderItemEntity 集合
     *
     * @return
     */
    private List<OrderItemEntity> createOrderItems(String orderSn) {
        List<OrderItemVo> orderItems = cartFeignService.getCurrentCartItems();
        return orderItems.stream().map(orderItemVo -> {
            OrderItemEntity orderItemEntity = createOrderItem(orderItemVo);
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());
    }

    /**
     * 创建 一个购物项
     * 提交了 OrderItemVo
     * 需要 OrderItemEntity
     * 映射封装重新查询
     *
     * @param orderItemVo
     * @return
     */
    private OrderItemEntity createOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //1、商品的spu信息
        Long skuId = orderItemVo.getSkuId();
        R<SpuInfoVo> spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoData = spuInfoBySkuId.getDate(new TypeReference<SpuInfoVo>() {
        });

        // 封装spu 数据
        orderItemEntity.setSpuId(spuInfoData.getId());
        orderItemEntity.setSpuName(spuInfoData.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoData.getBrandName());
        orderItemEntity.setCategoryId(spuInfoData.getCatalogId());

        //2、封装sku 数据
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImage());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());

        //将list集合转换为String
        String skuAttrValues = StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrValues);

        //3、商品的优惠信息

        //4、商品的积分信息  //价格 * 数量
        int gift = orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue();
        orderItemEntity.setGiftGrowth(gift);
        orderItemEntity.setGiftIntegration(gift);

        //5、订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        //当前订单项的实际金额.总额 - 各种优惠价格
        //原来的价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        //原价减去优惠价得到最终的价格
        BigDecimal subtract = origin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

    /**
     * 获取收货数据 封装订单基本信息
     *
     * @param addrId
     * @return
     */
    private OrderEntity createOrderEntity(Long addrId) {
        OrderEntity orderEntity = new OrderEntity();

        // 2、传入收货地址Id 获取收货地址数据和运费
        FareVo fareVo = wmsFeignService.getFare(addrId)
                .getDate(new TypeReference<FareVo>() {
                });
        // 运费封装
        orderEntity.setFreightAmount(fareVo.getFare());
        // 收货人数据封装
        MemberAddressVo address = fareVo.getAddress();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setConfirmStatus(0);
        return orderEntity;
    }


    /**
     * 创建订单业务中
     * 校验提交上来的token
     *
     * @param orderToken 订单token
     * @param userId     用户Id
     * @return
     */
    private Long checkOrderToken(String orderToken, Long userId) {
        //1、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //2、通过lure脚本原子验证令牌和删除令牌
        return stringRedisTemplate.execute(
                //封装脚本
                new DefaultRedisScript<Long>(script, Long.class),
                // redis 的key
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId),
                // 比较的值
                orderToken);
    }

    /**
     * 修改订单状态
     * @param orderSn
     * @param code
     */
    private void updateOrderStatus(String orderSn, Integer code,Integer payType) {

        this.baseMapper.updateOrderStatus(orderSn,code,payType);
    }
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );
        return new PageUtils(page);
    }

}