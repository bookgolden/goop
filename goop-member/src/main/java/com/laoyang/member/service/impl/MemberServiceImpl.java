package com.laoyang.member.service.impl;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.Query;
import com.laoyang.common.util.R;
import com.laoyang.member.dao.MemberDao;
import com.laoyang.member.entity.MemberEntity;
import com.laoyang.member.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.laoyang.member.to.vo.LoginVo;
import com.laoyang.member.to.vo.RegisVo;
import com.laoyang.member.util.exception.PhoneExistException;
import com.laoyang.member.util.exception.UserNameExistException;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    /**
     * 注册
     *
     * @param regisvo
     */
    @Override
    public void userRegis(RegisVo regisvo) {
        userNameCheck(regisvo.getUsername());
        userNameCheck(regisvo.getPhone());

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(regisvo.getUsername());
        memberEntity.setMobile(regisvo.getPhone());

        //TODO 密码存储处理
        String password = new BCryptPasswordEncoder().encode(regisvo.getPassword());
        memberEntity.setPassword(password);

        // 保存
        baseMapper.insert(memberEntity);
    }

    @Override
    public R userLogin(LoginVo loginVo) {
        MemberEntity memberEntity = null;

        if (StringUtils.isNotEmpty(loginVo.getLoginacct())) {
            memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                    .eq("username", loginVo.getLoginacct()).or()
                    .eq("mobile",loginVo.getLoginacct()));
        }
        if (memberEntity == null) {
            return R.error(400,"用户不存在");
        }
        //密码校验
        boolean res = new BCryptPasswordEncoder()
                .matches(loginVo.getPassword(), memberEntity.getPassword());

        if (res) {
            //将登录的用户存入返回结果集
            return R.ok().put("data",memberEntity);
        }
        return R.error(400,"密码错误");
    }

    /**
     * username已存在校验
     *
     * @param username
     */
    private void userNameCheck(String username) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    /**
     * phone已存在校验
     *
     * @param phone
     */
    private void phoneCheck(String phone) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

}