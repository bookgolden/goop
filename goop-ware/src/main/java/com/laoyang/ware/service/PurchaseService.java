package com.laoyang.ware.service;

import com.laoyang.common.util.PageUtils;
import com.laoyang.ware.entity.PurchaseEntity;
import com.laoyang.ware.vo.MergeVo;
import com.laoyang.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);


    void mergePurchase(MergeVo mergeVo);


    void received(List<Long> ids);


    void done(PurchaseDoneVo doneVo);


}

