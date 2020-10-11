package com.laoyang.search;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yyy
 * @Date 2020-08-08 20:06
 * @Email yangyouyuhd@163.com
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class SuggestTest {

    @Autowired
    RestHighLevelClient client;


    @SneakyThrows
    @Test
    public void suggest(){

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autocomplete",SuggestBuilders.completionSuggestion("suggest").prefix("华"));

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.suggest(suggestBuilder);
        SearchResponse response = this.client.search(new SearchRequest("music").source(builder), RequestOptions.DEFAULT);
        Suggest suggest = response.getSuggest();
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion
                = suggest.getSuggestion("autocomplete");
        CompletionSuggestion completionSuggestion = (CompletionSuggestion)suggestion;
        List<CompletionSuggestion.Entry.Option> options = completionSuggestion.getOptions();

        for (CompletionSuggestion.Entry.Option option : options) {
            // 获取联想出来的关键字
            String text = option.getText().toString();
            // 获取关键字所处的纪录
            SearchHit hit = option.getHit();
            // 不明觉厉
            Map<String, Set<String>> contexts = option.getContexts();
        }
    }

    @SneakyThrows
    @Test
    public void prefix(){
        String prefix = "华为";
        QueryBuilder query = QueryBuilders.prefixQuery("brandName", prefix);

        SearchResponse response = client.search(
                new SearchRequest("product")
                        .source(new SearchSourceBuilder().query(query)
                                .fetchSource(new String[]{"brandName"}, new String[]{})),
                RequestOptions.DEFAULT);

        SearchHit[] hits = response.getHits().getHits();
        List<String> list = Arrays.stream(hits)
                .map(item -> (String)item.getSourceAsMap().get("brandName"))
                .distinct()
                .collect(Collectors.toList());
        System.out.println(list);
        //        builder.query(searchRequest);
//        this.client
    }
}
