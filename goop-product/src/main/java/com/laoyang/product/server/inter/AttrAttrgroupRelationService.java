package com.laoyang.product.server.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.util.PageUtils;
import com.laoyang.product.entity.AttrAttrgroupRelationEntity;
import com.laoyang.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGroupRelationVo> vos);

}

