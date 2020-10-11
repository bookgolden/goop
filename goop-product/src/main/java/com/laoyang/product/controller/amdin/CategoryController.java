package com.laoyang.product.controller.amdin;

import com.laoyang.common.util.R;
import com.laoyang.product.entity.CategoryEntity;
import com.laoyang.product.server.inter.CategoryService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


/**
 * 商品三级分类
 */
@RestController
@RequestMapping("product/category")
public class    CategoryController {
    @Resource
    private CategoryService categoryService;


    @RequestMapping("/list/tree")
    @ApiOperation(value = "查询三级分类、组装为分类树",notes = "前端通过key==data获取")
    public R list(){
        List<CategoryEntity> entities = categoryService.listWithTree();
        return R.ok().put("data", entities);
    }


    @ApiImplicitParam(name = "catId",value = "分类的Id",required = true,paramType = "long")
    @ApiOperation(value = "获取该分类的详细信息",notes = "前端通过key==data获取")
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }


    @ApiImplicitParam(name = "category",value = "分类json数据",required = true)
    @ApiOperation(value = "保存一条分类数据")
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    @ApiImplicitParam(name = "category",value = "一组分类实体数组",required = true)
    @ApiOperation(value = "修改一组分类数据")
    @RequestMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }


    @RequestMapping("/update")
    @ApiImplicitParam(name = "category",value = "一条分类实体",required = true)
    @ApiOperation(value = "修改一条分类数据")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateCascade(category);
        return R.ok();
    }



    @RequestMapping("/delete")
    @ApiImplicitParam(name = "catIds",value = "待删除的分类Id的集合",required = true)
    @ApiOperation(value = "删除一组分类数据")
    public R delete(@RequestBody Long[] catIds){
		//categoryService.removeByIds(Arrays.asList(catIds));

        categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
