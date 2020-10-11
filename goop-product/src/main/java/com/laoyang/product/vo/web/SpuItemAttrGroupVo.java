package com.laoyang.product.vo.web;

import com.laoyang.product.vo.Attr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-06-30 15:39
 * @Email yangyouyuhd@163.com
 * @Note
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public  class SpuItemAttrGroupVo{
    private String groupName;
    private List<Attr> attrs;
}
