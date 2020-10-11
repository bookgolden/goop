package com.laoyang.product.server.inter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.laoyang.common.util.PageUtils;
import com.laoyang.product.entity.CategoryEntity;
import com.laoyang.product.vo.web.Catalog2Vo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 */
@Service
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);


    /**
     * 找到catelogId的完整路径；
     * [父/子/孙]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> listOneCategory(Long parentId);

    Map<String,List<Catalog2Vo>> listCategoryTree();
}

