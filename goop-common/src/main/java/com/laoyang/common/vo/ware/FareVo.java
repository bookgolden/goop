package com.laoyang.common.vo.ware;

import com.laoyang.common.vo.member.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yyy
 * @Date 2020-07-15 15:02
 * @Email yangyouyuhd@163.com
 */
@Data
public class FareVo {


    private MemberAddressVo address;

    private BigDecimal fare;
}

