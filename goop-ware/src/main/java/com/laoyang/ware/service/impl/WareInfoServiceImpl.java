package com.laoyang.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.member.MemberAddressVo;
import com.laoyang.ware.dao.WareInfoDao;
import com.laoyang.ware.entity.WareInfoEntity;
import com.laoyang.ware.entity.WareOrderTaskDetailEntity;
import com.laoyang.ware.feign.MemberFeignService;
import com.laoyang.ware.service.WareInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.common.vo.ware.FareVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {


    @Resource
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        //收获地址的详细信息
        R<MemberAddressVo> addrInfo = memberFeignService.info(addrId);
        MemberAddressVo memberAddressVo = addrInfo.get("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        if (memberAddressVo != null) {
            String phone = memberAddressVo.getPhone();
            // 获取最后一位
            String fare = phone.substring(phone.length() - 1);
            BigDecimal bigDecimal = new BigDecimal(fare);

            fareVo.setFare(bigDecimal);
            fareVo.setAddress(memberAddressVo);
        }
        return fareVo;
    }




}