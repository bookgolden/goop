package com.laoyang.product.controller.web;

import com.laoyang.product.entity.CategoryEntity;
import com.laoyang.product.server.inter.CategoryService;
import com.laoyang.product.vo.web.Catalog2Vo;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Date 2020-06-28 16:21
 * @Created laoyang
 * @Email yangyouyuhd@163.com
 */
@Controller
public class MainController {

    @Resource
    CategoryService categoryService;

    @GetMapping({"/", "index"})
    @ApiOperation(value = "查询出一级分类、渲染首页")
    public String index(Model model) {
        List<CategoryEntity> categoryEntityList = categoryService.listOneCategory(0L);
        model.addAttribute("category", categoryEntityList);
        return "index";
    }


    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    @ApiOperation(value = "首页三级分类点击自动渲染", notes = "首页渲染完毕后、AJAX调用该接口")
    public Map<String, List<Catalog2Vo>> categoryTree() {
        Map<String, List<Catalog2Vo>> val = categoryService.listCategoryTree();
        return val;
    }

    /**
     * {
     *     "1": [
     *         {
     *             "catalog1Id": "1",
     *             "catalog3List": [
     *                 {
     *                     "catalog2Id": "2",
     *                     "id": "3",
     *                     "name": "3"
     *                 }
     *             ],
     *             "id": "2",
     *             "name": "2"
     *         }
     *     ]
     * }
     */

}

