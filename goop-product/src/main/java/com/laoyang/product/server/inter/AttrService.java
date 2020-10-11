package com.laoyang.product.server.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.util.PageUtils;
import com.laoyang.product.entity.AttrEntity;
import com.laoyang.product.vo.AttrGroupRelationVo;
import com.laoyang.product.vo.AttrRespVo;
import com.laoyang.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<AttrEntity> selectSearchAttr(List<Long> allAttrIdList);
}

