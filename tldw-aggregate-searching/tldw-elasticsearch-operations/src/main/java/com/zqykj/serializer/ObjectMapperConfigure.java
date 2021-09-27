package com.zqykj.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/24
 */
@Configuration
public class ObjectMapperConfigure {
    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(simpleModule());
        return objectMapper;
    }

    private SimpleModule simpleModule() {
        ParsedStringTermsBucketSerializer serializer = new ParsedStringTermsBucketSerializer(ParsedStringTerms.ParsedBucket.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(serializer);
        return module;
    }
}