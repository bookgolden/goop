package com.laoyang.product.controller.amdin;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.R;
import com.laoyang.product.entity.ProductAttrValueEntity;
import com.laoyang.product.server.inter.ProductAttrValueService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * SPU 公共属性值
 */
@RestController
@RequestMapping("product/productattrvalue")
public class ProductAttrValueController {

    @Resource
    private ProductAttrValueService productAttrValueService;


    @RequestMapping("/list")
    @ApiImplicitParam(name = "params",value = "待撒选的条件",required = true)
    @ApiOperation(value = "获取SPU的公共属性值列表、分页",notes = "前端通过key==page获取")
    public R list(@RequestParam Map<String, Object> params){

        PageUtils page = productAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }



    @RequestMapping("/info/{id}")
    @ApiImplicitParam(name = "params",value = "待撒选的条件",required = true)
    @ApiOperation(value = "获取指定SPU商品的公共属性值",notes = "前端通过key==productAttrValue获取")
    public R info(@PathVariable("id") Long id){
		ProductAttrValueEntity productAttrValue = productAttrValueService.getById(id);

        return R.ok().put("productAttrValue", productAttrValue);
    }


    @RequestMapping("/save")
    @ApiImplicitParam(name = "productAttrValue",value = "待保存的属性值",required = true)
    @ApiOperation(value = "保存一条属性值到SPU")
    public R save(@RequestBody ProductAttrValueEntity productAttrValue){
		productAttrValueService.save(productAttrValue);

        return R.ok();
    }


    @RequestMapping("/update")
    @ApiImplicitParam(name = "productAttrValue",value = "待修改的属性值",required = true)
    @ApiOperation(value = "修改一条属性值到SPU")
    public R update(@RequestBody ProductAttrValueEntity productAttrValue){
		productAttrValueService.updateById(productAttrValue);

        return R.ok();
    }

    @RequestMapping("/delete")
    @ApiImplicitParam(name = "ids",value = "待删除的属性值ids",required = true)
    @ApiOperation(value = "删除多条属性值在SPU")
    public R delete(@RequestBody Long[] ids){
		productAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
