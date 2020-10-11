package com.laoyang.coupon.service.impl;

import com.laoyang.common.to.SkuReductionTo;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.coupon.dao.CouponHistoryDao;
import com.laoyang.coupon.entity.CouponHistoryEntity;
import com.laoyang.coupon.service.CouponHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("couponHistoryService")
public class CouponHistoryServiceImpl extends ServiceImpl<CouponHistoryDao, CouponHistoryEntity> implements CouponHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CouponHistoryEntity> page = this.page(
                new Query<CouponHistoryEntity>().getPage(params),
                new QueryWrapper<CouponHistoryEntity>()
        );

        return new PageUtils(page);
    }

}