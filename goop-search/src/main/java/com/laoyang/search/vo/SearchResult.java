package com.laoyang.search.vo;

import com.laoyang.common.vo.es.SkuVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-06-28 18:48
 * @Email yangyouyuhd@163.com
 * @Note 搜索的结果封装
 */
@Data
public class SearchResult {
    /**
     * 所有商品
     */
    private List<SkuVO> products;

    /**
     * 当前页码、总记录数、总页码
     */
    private Integer pageNum;
    private Long total;
    private Integer totalPages;

    /**
     * 结果类型所涉及的品牌
     */
    private List<BrandVo> brands;
    /**
     * 分类
     */
    private List<AttrVo> attrs;
    /**
     * 所涉及的属性
     */
    private List<CatalogVo> catalogs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}

