package com.laoyang.search.server.impl;

import com.alibaba.fastjson.JSON;
import com.laoyang.common.vo.es.SkuVO;
import com.laoyang.search.config.Constant;
import com.laoyang.search.server.ProductService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    RestHighLevelClient client;

    @Override
    public boolean productStatusUp(List<SkuVO> skuVOList) {

        BulkRequest bulk = new BulkRequest();

        for (SkuVO sku : skuVOList) {
            IndexRequest indexRequest = new IndexRequest(Constant.PRODUCT_INDEX);

            indexRequest.id(sku.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(sku), XContentType.JSON);

            bulk.add(indexRequest);
        }
        BulkResponse responses = null;
        try {
            responses = client.bulk(bulk, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(responses.hasFailures()){
            log.error("批量索引出现错误。。。。。。。{}",responses.buildFailureMessage());
            BulkItemResponse[] items = responses.getItems();
            Arrays.stream(items).map(item->{
                System.out.println(item);
                return null;
            });
        }
        return true;
    }
}
