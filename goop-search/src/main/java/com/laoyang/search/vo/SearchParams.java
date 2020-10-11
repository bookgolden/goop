package com.laoyang.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-06-28 17:38
 * @Email yangyouyuhd@163.com
 * @Note 前台搜索参数vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParams {

    //搜索关键字
    private String keyword;
    //选择分类
    private Long catalog3Id;
    //仅看有库存
    private Integer hasStock;
    /**
     * 包含价格区间上下限、用_连接
     *  lte_gte
     *  _gte
     *  lte_
     */
    private String skuPrice;
    //多选的品牌Id
    private List<Long> brandId;
    /**
     * 包含具体的属性名和多个值
     * 属性名_属性值  属性值1:属性值2
     * 尺寸_15:16:17
     * 内存_8:16:32
     */
    private List<String> attrs;

    //当前页码
    private Integer pageNum ;
    /**
     *  sort=saleCount_asc/desc
     *  sort=skuPrice_asc/desc
     *  sort=hotScore_asc/desc
     */
    private String sort;

}
