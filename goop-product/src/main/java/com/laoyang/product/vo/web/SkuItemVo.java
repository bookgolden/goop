package com.laoyang.product.vo.web;

import com.laoyang.common.vo.coupon.SecKillSessionVo;
import com.laoyang.common.vo.seckill.SecKillSkuRedisVo;
import com.laoyang.product.entity.SkuImagesEntity;
import com.laoyang.product.entity.SkuInfoEntity;
import com.laoyang.product.entity.SpuInfoDescEntity;
import com.laoyang.product.vo.Attr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-06-30 15:03
 * @Email yangyouyuhd@163.com
 * @Note 商品详情的返回视图
 */
@Data
public class SkuItemVo {

    private boolean hasStock = true;

    /**
     *  sku基本信息、pms_sku_info
     */
    private SkuInfoEntity skuInfo;

    /**
     * sku图片信息、pms_sku_images
     */
    private List<SkuImagesEntity> images;

    /**
     * spu详情信息 pms_spu_desc
     */
    private SpuInfoDescEntity spuInfoDesc;

    /**
     * spu销售属性组合、
     */
    private List<SkuItemSaleAttrVo> saleAttrs;

    /**
     * spu属性组集合
     */
    private List<SpuItemAttrGroupVo> apuItemAttrGroups;

    /**
     * 秒杀商品的优惠信息
     */
    private SecKillSkuRedisVo secKillSkuRedisVo;
}
