package com.laoyang.ware.service;

import com.laoyang.common.to.mq.StockLockedTo;
import com.laoyang.common.to.order.OrderTo;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.vo.ware.WareSkuLockVo;
import com.laoyang.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.vo.ware.SkuStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);


    List<SkuStockVo> selectSkuStock(List<Long> skuIds);

    /**
     * 锁定库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     *  解锁库存
     * @param skuId
     * @param wareId
     * @param skuNum
     * @param id
     */
    void unLockStock(Long skuId, Long wareId, Integer skuNum, Long id);

    /**
     *  处理订单库存释放
     * @param stockLockedTo
     * @return
     */
    boolean handleOrderStockRelease(StockLockedTo stockLockedTo);

    /**
     * 处理订单未支付关单
     * @param orderTo
     * @return
     */
    void handleOrderCloseRelease(OrderTo orderTo);
}

