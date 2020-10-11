package com.laoyang.ware.service;

import com.laoyang.common.util.PageUtils;
import com.laoyang.ware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.vo.ware.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取运费和收货地址信息
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);


}

