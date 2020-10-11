package com.laoyang.auth.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.laoyang.auth.config.AuthConstant;
import com.laoyang.auth.feign.MemberService;
import com.laoyang.auth.feign.ThirdSmsService;
import com.laoyang.auth.xo.to.RegisTo;
import com.laoyang.auth.xo.vo.UserLoginVo;
import com.laoyang.auth.xo.vo.UserRegisVo;
import com.laoyang.common.util.R;
import com.laoyang.common.vo.user.MemberSessionTo;
import com.laoyang.common.vo.ware.SkuStockVo;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yyy
 * @Date 2020-07-02 20:17
 * @Email yangyouyuhd@163.com
 * @Note
 */
@Controller
public class LoginController {

    @Resource
    ThirdSmsService smsService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    MemberService memberService;


    /**
     * 注册页验证码发送处理接口
     * 1、重复校验
     * 2、接口防刷
     *
     * @param phone
     */
    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        ValueOperations<String, String> forValue = stringRedisTemplate.opsForValue();
        String val = forValue.get(AuthConstant.SMS_CODE + phone);

        if (StringUtils.isEmpty(val)) {
            //初次调用、发送验证码
            String code = RandomUtil.randomNumbers(6);
            System.out.println("code:" + code);
            R res = smsService.sendCode(phone, code);

            //并存入redis
            RegisTo regisTo = new RegisTo(phone, code, System.currentTimeMillis());
            forValue.set(AuthConstant.SMS_CODE + phone, JSON.toJSONString(regisTo), 60, TimeUnit.SECONDS);
            return res;
        }

        //不为空、重复调用、调用时长校验
        RegisTo to = JSON.parseObject(val, RegisTo.class);
        if (System.currentTimeMillis() - to.getCurTime() < 60000) {
            //60s内重复调用、滚粗
            return R.error();
        } else {
            //已经过去了60s、重复发送验证码
            String code = RandomUtil.randomNumbers(6);
            smsService.sendCode(phone, code);

            //刷新redis缓存
            to.setCurTime(System.currentTimeMillis());
            forValue.set(AuthConstant.SMS_CODE + phone, JSON.toJSONString(to), 10, TimeUnit.MINUTES);
            return R.ok();
        }
    }

    /**
     * Ajax 表单提交异步校验、
     * 1、数据格式校验
     * 2、验证码校验
     * 3、调用远程会员服务存储会员信息
     *
     * @param userRegisVo
     * @param bindingResult
     * @return
     */
    @PostMapping("/user/regis")
    public String regis(@Valid UserRegisVo userRegisVo,
                        BindingResult bindingResult,
                        RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            //TODO 提交的数据有格式错误、校验失败
            Map<String, String> err = bindingResult.getFieldErrors()
                    .stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", err);
            return "redirect:http://auth.goop.com/to/regis";
        }
        /**
         * 校验验证码、从redis
         * 为空、验证码已过期
         * 非指定值、验证码错误
         * 成功、删除验证码
         */
        ValueOperations<String, String> forValue = stringRedisTemplate.opsForValue();
        String val = forValue.get(AuthConstant.SMS_CODE + userRegisVo.getPhone());
        if (StringUtils.isEmpty(val)) {
            //验证码已过期
            attributes.addFlashAttribute("errors", R.ok().put("code", "验证码以过期"));
            return "redirect:http://auth.goop.com/to/regis";
        } else if (!userRegisVo.getCode().equals(JSON.parseObject(val, RegisTo.class).getCode())) {
            //用户提交的验证码 不是redis保存的验证码、
            attributes.addFlashAttribute("errors", R.ok().put("code", "验证码错误"));
            return "redirect:http://auth.goop.com/to/regis";
        }

        // TODO 、验证码通过、删除redis纪录、调用远程会员服务、准备保存
        stringRedisTemplate.delete(AuthConstant.SMS_CODE + userRegisVo.getPhone());
        R res = memberService.regis(userRegisVo);
        System.out.println(res);
        return "redirect:http://auth.goop.com/to/login";
    }

    /**
     * Ajax 异步登录校验
     *
     * @param loginVo
     * @return
     */
    @PostMapping("/login")
//    @ResponseBody
    @ApiOperation(value = "登录接口", notes = "接受用户名或密码登录")
    public String login(UserLoginVo loginVo, RedirectAttributes attributes, HttpSession session) {
        R<MemberSessionTo> res = memberService.login(loginVo);

        if (res.getCode() == 200) {
            //从结果集中取出当前登录用户并存储session(redis)
            MemberSessionTo date = res.getDate(new TypeReference<MemberSessionTo>() {});

            session.setAttribute("loginUser", date);

            return "redirect:http://goop.com";
        } else {
            attributes.addFlashAttribute("errors", res);
            return "redirect:http://auth.goop.com/to/login";
        }
    }
}
