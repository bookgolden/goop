package com.laoyang.search.controller.web;

import com.google.common.collect.Lists;
import com.laoyang.search.server.GoopSearchService;
import com.laoyang.search.vo.SearchParams;
import com.laoyang.search.vo.SearchResult;
import lombok.SneakyThrows;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionSearchContext;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.ListUtils;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

@Controller
public class SearchController {

    @Resource
    GoopSearchService searchService;


    /**
     *  用户输入关键词时、
     *      不断根据前缀联想、
     * @param prefix
     * @return
     */
    @SneakyThrows
    @GetMapping("/searchSuggest")
    @ResponseBody
    public String[] searchSuggest(String prefix){
        System.out.println(prefix);
        //        List<String> list = searchService.searchSuggest(prefix);
        return  new String[]{"华为p30","华为mate20"};
    }

    /**
     * 首页关键字检索处理接口
     * @param searchParams
     * @param model
     * @return
     */
    @GetMapping("/list.html")
    public String searchMainPage(SearchParams searchParams, Model model) {
        //1、根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = searchService.keywordSearch(searchParams);

        model.addAttribute("result",result);

        return "list";
    }


    /**
     * 详情页检索处理接口
     * @param params    接收的参数集合
     * @param model     //
     * @return
     */
    @GetMapping("/search")
//    @ResponseBody
    public Object search(SearchParams params, Model model) {
        SearchResult searchResult = searchService.searchParams(params);
        model.addAttribute("result", searchResult);
        return "list";
    }
}
