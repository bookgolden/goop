package com.laoyang.coupon.service.impl;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.common.vo.coupon.SecKillSessionVo;
import com.laoyang.common.vo.coupon.SecKillSkuRelationVo;
import com.laoyang.coupon.dao.SeckillSessionDao;
import com.laoyang.coupon.entity.SeckillSessionEntity;
import com.laoyang.coupon.entity.SeckillSkuRelationEntity;
import com.laoyang.coupon.service.SeckillSessionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    SeckillSkuRelationService seckillSkuRelationService;


    /**
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.eq("id", key);
        }

        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SecKillSessionVo> getLate3DaySession() {
        QueryWrapper<SeckillSessionEntity> wrapper = new QueryWrapper();
        wrapper.between("start_time", startTime(), endTime());
        List<SeckillSessionEntity> seckillSessionEntities = this.list(wrapper);

        /**
         *  查询并封装 SeckillSkuRelationEntity
         *  将 数据载体 entity -> vo
         */
        List<SecKillSessionVo> resList = Collections.EMPTY_LIST;
        if (seckillSessionEntities != null && !seckillSessionEntities.isEmpty()) {
            resList= seckillSessionEntities.stream().map(item -> {
                // 将SeckillSessionEntity -> SecKillSessionVo
                SecKillSessionVo res = new SecKillSessionVo();
                BeanUtils.copyProperties(item, res);

                // 将 SeckillSkuRelationEntity -> SecKillSkuRelationVo
                List<SeckillSkuRelationEntity> killSkuEntityList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", item.getId()));
                List<SecKillSkuRelationVo> KillSkuVoList = new ArrayList<>();

                killSkuEntityList.stream().forEach( SeckillSkuRelationEntity-> {
                    SecKillSkuRelationVo secKillSkuRelationVo = new SecKillSkuRelationVo();
                    BeanUtils.copyProperties(SeckillSkuRelationEntity, secKillSkuRelationVo);
                    KillSkuVoList.add(secKillSkuRelationVo);
                });
                res.setSecKillSkuRelationVoList(KillSkuVoList);
                return res;
            }).collect(Collectors.toList());
        }

        return resList;
    }


    /**
     * 当前时间
     *
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);

        //格式化时间
        String startFormat = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return startFormat;
    }

    /**
     * 结束时间
     *
     * @return
     */
    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus, max);

        //格式化时间
        String endFormat = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return endFormat;
    }


}