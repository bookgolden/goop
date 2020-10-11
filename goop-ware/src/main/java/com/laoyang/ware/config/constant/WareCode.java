package com.laoyang.ware.config.constant;

/**
 * @author yyy
 * @Date 2020-07-15 22:20
 * @Email yangyouyuhd@163.com
 */
public enum  WareCode {
    NoStock(11000,"库存不足"),

    ;

    private int code;

    private String msg;

    WareCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
