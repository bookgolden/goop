package com.laoyang.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.laoyang.common.enums.order.OrderStatusEnum;
import com.laoyang.common.to.mq.StockDetailTo;
import com.laoyang.common.to.mq.StockLockedTo;
import com.laoyang.common.to.order.OrderTo;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.order.OrderItemVo;
import com.laoyang.common.vo.order.OrderVo;
import com.laoyang.common.vo.ware.WareSkuLockVo;
import com.laoyang.ware.config.excep.NoStockException;
import com.laoyang.ware.dao.WareSkuDao;
import com.laoyang.ware.entity.WareOrderTaskDetailEntity;
import com.laoyang.ware.entity.WareOrderTaskEntity;
import com.laoyang.ware.entity.WareSkuEntity;
import com.laoyang.ware.feign.OrderFeignService;
import com.laoyang.ware.feign.ProductFeignService;
import com.laoyang.ware.service.WareOrderTaskDetailService;
import com.laoyang.ware.service.WareOrderTaskService;
import com.laoyang.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.common.vo.ware.SkuStockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author yyy
 */
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Resource
    WareOrderTaskService wareOrderTaskService;


    @Resource
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    OrderFeignService orderFeignService;

    /**
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuStockVo> selectSkuStock(List<Long> skuIds) {

        List<SkuStockVo> skuStockVos = skuIds.stream().map(skuId -> {
            SkuStockVo skuStockVo = new SkuStockVo();

            WareSkuEntity wareSkuEntity = baseMapper.selectSkuStock(skuId);
            if (wareSkuEntity != null) {
                skuStockVo.setSkuId(wareSkuEntity.getSkuId());
                skuStockVo.setStockTotal(Long.valueOf(wareSkuEntity.getStock()));
            }else {
                // 找不到当前商品的纪录、给个默认值吧！
                skuStockVo.setSkuId(skuId);
                skuStockVo.setStockTotal(10L);
            }
            return skuStockVo;
        }).collect(Collectors.toList());

        return skuStockVos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单详情信息
         *  后续可能释放库存
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 需要锁库存的订单项集合
        List<OrderItemVo> locks = vo.getLocks();

        /**
         *  1、找到每个商品在哪些仓库都有库存
         */
        List<SkuWareHasStock> collect = locks.stream().map((item) -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪个仓库有库存
            List<Long> wareIdList = this.baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIdList);
            return stock;
        }).collect(Collectors.toList());


        /**
         * 2、遍历每个商品库存充足的仓库集合、试图锁库存
         * 3、如果要就近锁库存、应该对wareId 集合 进行仓库地址距离升序
         */
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();

            if (org.springframework.util.StringUtils.isEmpty(wareIds)) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException((Integer.valueOf(skuId.toString())));
            }

            for (Long wareId : wareIds) {
                //锁定成功就返回1，失败就返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    /**
                     *  锁库存成功、发送延时消息、不受异常回滚
                     *  如果每一个商品都锁定成功,将当前商品锁定了几件的工作单记录发给MQ
                     *  锁定失败。前面保存的工作单信息都回滚了。发送出去的消息，即使要解锁库存，由于在数据库查不到指定的id，所有就不用解锁
                     */

                    // 组装消息
                    StockDetailTo detailTo = new StockDetailTo(
                            null,skuId,null,hasStock.getNum(),
                            wareOrderTaskEntity.getId(),wareId,1);
                    StockLockedTo stockLockedTo = new StockLockedTo(wareOrderTaskEntity.getId(),detailTo);
                    // 发送
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);

                    // 封装锁库存详情纪录
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
                    BeanUtils.copyProperties(detailTo,entity);
                    wareOrderTaskDetailService.save(entity);

                    skuStocked = true;
                    break;
                }
            }
            if (!skuStocked) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException((Integer.valueOf(skuId.toString())));
            }
        }

        //3、肯定全部都是锁定成功的
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    /**
     * 解锁库存的方法
     * @param skuId
     * @param wareId
     * @param num
     * @param taskDetailId
     */
    @Override
    public void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId) {
        //库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num);

        //更新工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        //变为已解锁
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);
    }

    /**
     *  签收消息 true
     *  拒签消息 false
     * @param stockLockedTo
     * @return
     */
    @Override
    public boolean handleOrderStockRelease(StockLockedTo stockLockedTo){
        /**
         *  获取锁库详情纪录、
         *  并根据锁库详情纪录的Id 获取 db 里的纪录
         *  如果 db纪录为null、说明锁库存时发生回滚
         */
        StockDetailTo stockDetail = stockLockedTo.getDetailTo();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(stockDetail.getId());
        if(detailEntity == null){
            // 锁库详情纪录被回滚了、说明锁库时发生异常、无需继续解锁、可以批量签收
            return true;
        } else {
            /**
             *  1、根据锁库任务单Id查出 wms_ware_order_task锁库任务单的信息
             *  2、任务单保存了订单的Id、根据订单Id远程查询该条订单数据
             *  3、结果讨论
             *      如果查询订单纪录为null、说明库存锁定成功后、订单创建的后续逻辑发生错误、订单被回滚
             *      如果查询的订单记录为已取消、说明用户在规定时间内未支付、或者主动取消支付。
             */
            // 获取锁库任务单Id、
            Long taskId = stockLockedTo.getId();
            WareOrderTaskEntity orderTaskInfo = wareOrderTaskService.getById(taskId);
            String orderSn = orderTaskInfo.getOrderSn();
            R<OrderVo> orderVo = orderFeignService.getOneByOrderSn(orderSn);

            if (orderVo.getCode() == 200) {
                OrderVo orderInfo = orderVo.getDate( new TypeReference<OrderVo>() {});

                // 纪录为null、或锁库详情纪录状态为已取消 、都要解锁库存
                if (orderInfo == null || orderInfo.getStatus().equals(OrderStatusEnum.CANCEL.getCode())) {
                    //订单已被取消，才能解锁库存
                    if (detailEntity.getLockStatus() == 1) {
                        //锁库详情纪录状态1，已锁定，需要解锁
                        this.unLockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(), stockDetail.getId());
                    }else {
                        // 锁库详情纪录状态为 已解锁、或已扣减、无需继续解锁
                    }
                    return true;
                }else {
                    // 用户正常支付
                    return true;
                }
            } else {
                // 目前 不可能为 200 以外的code
                //  如果远程调用失败、消息返回队列、让别人试试
                return false ;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCloseRelease(OrderTo orderTo) {

        // 拿到订单号、获取最新的订单任务纪录
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        // 获取指定订单任务Id 下 所有 状态为未解锁的订单详情纪录集合
        Long taskId = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId).eq("lock_status", 1));


        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLockStock(
                    taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }
    }
}