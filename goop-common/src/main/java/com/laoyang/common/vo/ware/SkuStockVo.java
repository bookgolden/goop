package com.laoyang.common.vo.ware;

import lombok.Data;

/**
 * @author yyy
 */
@Data
public class SkuStockVo {

    private Long skuId;
    /**
     * 商品剩余库存
     */
    private Long stockTotal;
}
