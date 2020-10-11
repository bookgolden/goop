package com.laoyang.member.service;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.R;
import com.laoyang.member.entity.MemberEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.member.to.vo.LoginVo;
import com.laoyang.member.to.vo.RegisVo;

import java.util.Map;

/**
 * 会员
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 完成用户注册
     * @param regisvo
     */
    void userRegis(RegisVo regisvo);

    R userLogin(LoginVo loginVo);
}

