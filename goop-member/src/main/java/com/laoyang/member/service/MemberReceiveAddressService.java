package com.laoyang.member.service;

import com.laoyang.common.util.PageUtils;
import com.laoyang.member.entity.MemberReceiveAddressEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取用户的收货列表
     * @param memberId
     * @return
     */
    List<MemberReceiveAddressEntity> listReceiveAddress(Long memberId);
}

