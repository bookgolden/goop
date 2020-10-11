package com.laoyang.member.controller;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.R;
import com.laoyang.member.to.vo.LoginVo;
import com.laoyang.member.util.exception.PhoneExistException;
import com.laoyang.member.util.exception.UserNameExistException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.laoyang.member.entity.MemberEntity;
import com.laoyang.member.feign.CouponFeignService;
import com.laoyang.member.service.MemberService;
import com.laoyang.member.to.vo.RegisVo;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;


    @PostMapping("/regis")
    @ApiOperation(value = "用户注册调用接口、完成用户信息保存、如果校验通过的话")
    public R regis(@RequestBody RegisVo regisvo) {
        try {
            memberService.userRegis(regisvo);
            return R.ok();
        } catch (UserNameExistException e){
            return R.error(400,"用户名已存在！");
        }catch (PhoneExistException e){
            return R.error(400,"手机号已存在！");
        }catch (Exception e) {
            return R.error(500,e.getMessage());
        }
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录调用接口")
    public R login(@RequestBody LoginVo regisvo) {
        return memberService.userLogin(regisvo);
    }


    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {

        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
}
