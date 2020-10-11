package com.laoyang.search.controller;

import com.laoyang.common.util.R;
import com.laoyang.common.vo.es.SkuVO;
import com.laoyang.search.server.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * 关于商品索引ES
 */
@RequestMapping("/search/save")
@RestController
public class ProductController {


    @Resource
    ProductService productService;

    /**
     * 商品上架
     *      商品服务将数据组装ok后
     *      将其索引到索引库
     * @param skuVOList
     * @return
     */
    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuVO> skuVOList) {
        boolean res = productService.productStatusUp(skuVOList);
        return res ? R.ok() : R.error();
    }
}
