package com.laoyang.member.service;

import com.laoyang.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.util.PageUtils;
import java.util.Map;

/**
 * 会员等级
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

