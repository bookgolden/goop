package com.laoyang.coupon.service;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.vo.coupon.SecKillSessionVo;
import com.laoyang.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 * @author yyy
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {


    PageUtils queryPage(Map<String, Object> params);

    /**
     *  获取最近三天的秒杀场次
     * @return
     */
    List<SecKillSessionVo> getLate3DaySession();
}

