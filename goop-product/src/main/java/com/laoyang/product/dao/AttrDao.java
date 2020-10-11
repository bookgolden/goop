package com.laoyang.product.dao;

import com.laoyang.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    /**
     * 根据属性ids、获取可被检索的属性实体
     * @param allAttrIdList
     * @return
     */
    List<AttrEntity> selectSearchAttrs(@Param("attrIds") List<Long> allAttrIdList);
}
