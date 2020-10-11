package com.laoyang.product.Item;

import com.laoyang.product.dao.AttrGroupDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author yyy
 * @Date 2020-06-30 16:23
 * @Email yangyouyuhd@163.com
 * @Note
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class AttrGroup {

    @Resource
    AttrGroupDao attrGroupDao;
    @Test
    public void AttrGroupDao(){
//        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySkuId = attrGroupDao.getAttrGroupWithAttrsBySkuId(1L, 225L);
//        System.out.println(attrGroupWithAttrsBySkuId);
//        System.out.println("hello");
    }
}
