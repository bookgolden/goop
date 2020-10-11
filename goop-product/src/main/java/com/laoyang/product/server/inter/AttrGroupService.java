package com.laoyang.product.server.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.util.PageUtils;
import com.laoyang.product.entity.AttrGroupEntity;
import com.laoyang.product.vo.AttrGroupWithAttrsVo;
import com.laoyang.product.vo.web.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);


    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    /**
     * 根据传入的spuId 和所属三级分类Id、返回其对应的属性组
     * @param spuId
     * @param catalogId
     * @return
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySkuId(Long spuId, Long catalogId);
}

