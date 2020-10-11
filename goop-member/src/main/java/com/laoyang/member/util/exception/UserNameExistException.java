package com.laoyang.member.util.exception;

import java.util.concurrent.Executors;

/**
 * @author yyy
 * @Date 2020-07-03 16:59
 * @Email yangyouyuhd@163.com
 * @Note
 */
public class UserNameExistException extends RuntimeException {

    public UserNameExistException(){
        super("用户名已存在！");
    }
}
