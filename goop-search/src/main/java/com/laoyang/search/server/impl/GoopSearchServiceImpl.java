package com.laoyang.search.server.impl;

import com.alibaba.fastjson.JSON;
import com.laoyang.common.vo.es.SkuVO;
import com.laoyang.search.config.Constant;
import com.laoyang.search.server.GoopSearchService;
import com.laoyang.search.vo.SearchParams;
import com.laoyang.search.vo.SearchResult;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yyy
 * @Date 2020-06-28 19:14
 * @Email yangyouyuhd@163.com
 * @Note
 */
@Service
public class GoopSearchServiceImpl implements GoopSearchService {

    @Resource
    RestHighLevelClient client;


    @Override
    public SearchResult searchParams(SearchParams params) {
        //检索请求
        SearchRequest searchRequest = new SearchRequest(Constant.PRODUCT_INDEX);

        //根据参数构建检索器
        SearchSourceBuilder searchSourceBuilder = searchRequestBuilder(params);

        //TODO 聚合检索构建
        searchSourceBuilder = aggregationRequestBuilder(searchSourceBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO 结果处理
        return searchResultHandler(searchResponse, params);
    }

    @Override
    public SearchResult keywordSearch(SearchParams params) {
        if(params == null || params.getKeyword() == null){
            return new SearchResult();
        }
        //检索请求
        SearchRequest searchRequest = new SearchRequest(Constant.PRODUCT_INDEX);

        //根据参数构建检索器
        SearchSourceBuilder builder = searchRequestBuilder(params);

        builder = aggregationRequestBuilder(builder);

        searchRequest.source(builder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO 结果处理
        return searchResultHandler(searchResponse, params);
    }

    /**
     * 前缀搜索实现
     *  使用suggest Api实现、
     * @param prefix
     * @return
     */
    @SneakyThrows
    @Override
    public List<String> searchSuggest(String prefix) {

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        /**
         * 给本次建议取名autocomplete、后面要用
         * 根据已索引的字段名suggest 去搜索建议
         */
        suggestBuilder.addSuggestion(
                "autocomplete",
                SuggestBuilders.completionSuggestion("suggest")
                        .prefix(prefix));

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.suggest(suggestBuilder);
        SearchResponse response = client.search(
                new SearchRequest(Constant.PRODUCT_INDEX)
                        .source(builder),
                RequestOptions.DEFAULT);

        Suggest suggest = response.getSuggest();
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion
                = suggest.getSuggestion("autocomplete");
        // 强转子类
        CompletionSuggestion completionSuggestion = (CompletionSuggestion)suggestion;

        List<CompletionSuggestion.Entry.Option> options = completionSuggestion.getOptions();
        List<String> res = new ArrayList<>();
        for (CompletionSuggestion.Entry.Option option : options) {
            // 获取联想出来的关键字
            String text = option.getText().toString();
            // 获取关键字所处的纪录
            SearchHit hit = option.getHit();
            // 不明觉厉
            Map<String, Set<String>> contexts = option.getContexts();
            res.add(text);
        }
        return res;
    }


    /**
     * 根据参数集合、构建一个检索资源构建器
     *
     * @param params
     * @return
     */
    private SearchSourceBuilder searchRequestBuilder(SearchParams params) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //商品分类构建、term
        if (ObjectUtils.isNotEmpty(params.getCatalog3Id())) {
            boolQuery.filter(QueryBuilders.termQuery(Constant.CATALOG_ID, params.getCatalog3Id()));
        }

        //商品Id集合构建、terms
        if (ObjectUtils.isNotEmpty(params.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery(Constant.BRAND_ID, params.getBrandId()));
        }

        //如果还限制了很多属性、内嵌的
        if (ObjectUtils.isNotEmpty(params.getAttrs())) {

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (String attr : params.getAttrs()) {
                String[] a = attr.split("_");
                String[] attrValues = a[1].split(":");

                boolQueryBuilder.must(QueryBuilders.termsQuery(Constant.Attrs.attrId.toString(), a[0]));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery(Constant.Attrs.attrId.toString(), boolQueryBuilder, null);
                boolQuery.filter(nestedQuery);
            }

        }

        //只看有库存？
        if (ObjectUtils.isNotEmpty(params.getHasStock())) {
            boolQuery.filter(QueryBuilders.termsQuery(Constant.HAS_STOCK, params.getHasStock() == 1));
        }

        //价格查询拼装
        if (StringUtils.isNotEmpty(params.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(Constant.SKU_PRICE);
            String[] prices = params.getSkuPrice().split("_");
            //对价格区间组装查询
            if (params.getSkuPrice().startsWith("_")) {
                rangeQuery.lte(prices[0]);
            } else if (params.getSkuPrice().endsWith("_")) {
                rangeQuery.gte(prices[0]);
            } else {
                rangeQuery.gte(prices[0]).lte(prices[1]);
            }
            boolQuery.filter(rangeQuery);
        }

        //检索关键字构建
        if (StringUtils.isNotEmpty(params.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery(Constant.SKU_TITLE, params.getKeyword()));
        }


        //组装到搜索资源构建器
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(boolQuery);

        //排序构建
        if (StringUtils.isNotEmpty(params.getSort())) {
            String[] s = params.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0], order);
        }

        //分页
        builder.from(((params.getPageNum() == null ? 1 : params.getPageNum())  - 1) * Constant.pageSize);
        builder.size(Constant.pageSize);

        //高亮
        if (StringUtils.isNotEmpty(params.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(Constant.SKU_TITLE);
            highlightBuilder.preTags("<em style='color:red'>");
            highlightBuilder.postTags("</em>");
            builder.highlighter(highlightBuilder);
        }

        return builder;
    }

    /**
     * 根据参数集合、构建一个聚合检索构建器
     *
     * @param
     * @return
     */
    private SearchSourceBuilder aggregationRequestBuilder(SearchSourceBuilder builder) {
        /**
         * 构建品牌聚合、和其2个子聚合
         */
        TermsAggregationBuilder brandAgg = AggregationBuilders
                .terms("brand_agg")
                .field(Constant.BRAND_ID)
                .size(Constant.pageSize);
        TermsAggregationBuilder brandNameAgg = AggregationBuilders
                .terms("brand_name_agg")
                .field(Constant.BRAND_NAME)
                .size(1);
        TermsAggregationBuilder brandImgAgg = AggregationBuilders
                .terms("brand_Img_agg")
                .field(Constant.BRAND_IMG)
                .size(1);
        //组装
        brandAgg.subAggregation(brandNameAgg).subAggregation(brandImgAgg);

        builder.aggregation(brandAgg);

        //分类聚合catalog_name_agg
        TermsAggregationBuilder catalogAgg = AggregationBuilders
                .terms("catalog_agg")
                .field(Constant.CATALOG_ID)
                .size(Constant.pageSize);
        TermsAggregationBuilder catalogNameAgg = AggregationBuilders
                .terms("catalog_name_agg")
                .field(Constant.CATALOG_NAME)
                .size(Constant.pageSize);

        builder.aggregation(catalogAgg.subAggregation(catalogNameAgg));

        //属性聚合
        //TODO 嵌入式字段
        TermsAggregationBuilder attrIdAgg = AggregationBuilders
                .terms("attr_id_agg")
                .field(Constant.Attrs.attrId.toString())
                .size(Constant.pageSize);
        TermsAggregationBuilder attrNameAge = AggregationBuilders
                .terms("attr_name_age")
                .field(Constant.Attrs.attrName.toString())
                .size(1);
        TermsAggregationBuilder attrValueAge = AggregationBuilders
                .terms("attr_value_age")
                .field(Constant.Attrs.attrValue.toString())
                .size(Constant.pageSize);

        //构建父子关系
        attrIdAgg
                .subAggregation(attrNameAge)
                .subAggregation(attrValueAge);

        //构建嵌入式聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", Constant.ATTRS);
        attrAgg.subAggregation(attrIdAgg);
        builder.aggregation(attrAgg);

        return builder;
    }

    /**
     * 对检索结果 处理、
     *
     * @param searchResponse
     * @param params
     * @return
     */
    private SearchResult searchResultHandler(SearchResponse searchResponse, SearchParams params) {
        SearchResult result = new SearchResult();

        /**
         * 总命中数、当前页码、总页码封装
         */
        long totalHits = searchResponse.getHits().getTotalHits().value;
        Integer totalPages = (int) (totalHits % Constant.pageSize == 0 ? totalHits % Constant.pageSize : totalHits % Constant.pageSize + 1);
        result.setTotal(totalHits);
        result.setTotalPages(totalPages);
        result.setPageNum(params.getPageNum());

        /**
         * 命中结果映射封装
         */
        if (totalHits > 0) {
            List<SkuVO> products = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String source = hit.getSourceAsString();
                SkuVO skuVO = JSON.parseObject(source, SkuVO.class);
                if (StringUtils.isNotEmpty(params.getKeyword())) {
                    //封装高亮、有时不只有标题设置了高亮
                    for (Map.Entry<String, HighlightField> fieldEntry : hit.getHighlightFields().entrySet()) {
                        String key = fieldEntry.getKey();
                        String value = fieldEntry.getValue().getFragments()[0].toString();
                        if ("skuTitle".equalsIgnoreCase(key)) {
                            skuVO.setSkuTitle(value);
                        } else {
                            //TODO 都要判断进行映射、或者Switch
                        }
                    }
                }
                products.add(skuVO);
            }
            result.setProducts(products);
        }


        /**
         * 分类聚合映射封装
         */
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();

        try {
            ParsedLongTerms catalogAgg = searchResponse.getAggregations().get("catalog_agg");
            for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
                //获取父聚合分类Id、
                long catalogId = bucket.getKeyAsNumber().longValue();
                //获取子聚合分类名
                ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
                String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();

                //封装
                catalogVos.add(new SearchResult.CatalogVo(catalogId, catalogName));
            }
            result.setCatalogs(catalogVos);
        } catch (NullPointerException e) {
            e.getLocalizedMessage();
        }


        /**
         *  品牌聚合映射封装
         */
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        try {
            ParsedLongTerms brandAgg = searchResponse.getAggregations().get("brand_agg");
            for (Terms.Bucket bucket : brandAgg.getBuckets()) {
                //获取父聚合品牌名
                long brandId = bucket.getKeyAsNumber().longValue();
                //获取子聚合品牌名
                ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
                String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
                //获取子聚合品牌图片
                ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_Img_agg");
                String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();

                brandVos.add(new SearchResult.BrandVo(brandId, brandName, brandImg));
            }
        } catch (NullPointerException e) {
            e.getLocalizedMessage();
        }
        result.setBrands(brandVos);

        /**
         * 属性聚合映射封装
         */
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        try {
            ParsedNested attrAgg = searchResponse.getAggregations().get("attr_agg");

            ParsedStringTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");

            for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
                //属性Id、一个桶唯一
                long attrId = bucket.getKeyAsNumber().longValue();
                //属性名、一个桶唯一
                String attrName = ((ParsedStringTerms) bucket.getAggregations()
                        .get("attr_name_age")).getBuckets()
                        .get(0).getKeyAsString();
                //属性值、可能多个
                List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations()
                        .get("attr_value_age")).getBuckets()
                        .stream()
                        .map(innBucket -> innBucket.getKeyAsString())
                        .collect(Collectors.toList());

                attrVos.add(new SearchResult.AttrVo(attrId, attrName, attrValues));
            }
            result.setAttrs(attrVos);
        } catch (NullPointerException e) {
            e.getLocalizedMessage();
        }
        return result;
    }
}
