package com.laoyang.search.config;


import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class ClientConfig {


    @Resource
    RestClientBuilder restClientBuilder;

    /**
     * 配置高级客户端
     *
     * @return
     */
    @Bean
    public RestHighLevelClient createHighClient() {
        //高级客户端配置
        return new RestHighLevelClient(restClientBuilder);
    }

    @Bean
    public RestClientBuilder createLowClientBuilder() {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("192.168.56.10", 9200, "http"),
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http"));

        //配置超时回调接口、
        builder.setRequestConfigCallback(
                new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        System.out.println("超时了");
                        return requestConfigBuilder
                                .setConnectTimeout(5000)       //连接时间
                                .setSocketTimeout(10000);      //客户端时间
                    }
                });
        return builder;
    }
}
