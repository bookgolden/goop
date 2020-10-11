package com.laoyang.member.util.exception;

/**
 * @author yyy
 * @Date 2020-07-03 17:00
 * @Email yangyouyuhd@163.com
 * @Note
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException(){
        super("手机号已存在！");
    }
}
