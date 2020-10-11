package com.laoyang.product.server.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.util.PageUtils;
import com.laoyang.product.entity.SpuInfoEntity;
import com.laoyang.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity infoEntity);


    PageUtils queryPageByCondition(Map<String, Object> params);


    /**
     * 上架
     * @param spuId
     */
    void up(Long spuId);

    /**
     * 获取spuInfo by skuId
     * @param skuId
     * @return
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

