package com.laoyang.search.server;

import com.laoyang.search.vo.SearchParams;
import com.laoyang.search.vo.SearchResult;
import org.elasticsearch.action.search.SearchRequest;

import java.util.List;

/**
 * @author yyy
 * @Date 2020-06-28 19:12
 * @Email yangyouyuhd@163.com
 * @Note  对搜索条件与ES交互
 */
public interface GoopSearchService {


    /**
     * 根据前台搜索条件、检索ES、返回检索结果
     * @param params
     * @return
     */
    SearchResult searchParams(SearchParams params);

    /**
     * 首页输入搜索条件查询
     *      keyword
     * @param params
     * @return
     */
    SearchResult keywordSearch(SearchParams params);

    /**
     * 搜索提示补全
     *
     * @param prefix
     * @return
     */
    List<String> searchSuggest(String prefix);
}
