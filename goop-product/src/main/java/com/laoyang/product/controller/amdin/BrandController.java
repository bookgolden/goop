package com.laoyang.product.controller.amdin;

import com.laoyang.common.util.PageUtils;
import com.laoyang.common.util.R;
import com.laoyang.common.valid.AddGroup;
import com.laoyang.common.valid.UpdateGroup;
import com.laoyang.common.valid.UpdateStatusGroup;
import com.laoyang.product.entity.BrandEntity;
import com.laoyang.product.server.inter.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(
            @Validated({AddGroup.class})    //JSR分组校验、未实现逻辑
            @RequestBody BrandEntity brand){

        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(
            @Validated(UpdateGroup.class)
            @RequestBody BrandEntity brand){

		brandService.updateDetail(brand);
        return R.ok();
    }
    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(
            @Validated(UpdateStatusGroup.class)
            @RequestBody BrandEntity brand){

        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
