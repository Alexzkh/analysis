package com.zqykj.app.service.strategy.analysis.proxy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zqykj.infrastructure.util.JsonUtils;
import com.zqykj.infrastructure.util.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;

/**
 * @Description: 基础api代理类--分装api接口调用
 * @Author zhangkehou
 * @Date 2021/12/16
 */
@Component
public class BaseAPIProxy {


    private static final Logger logger = LoggerFactory.getLogger(BaseAPIProxy.class);


    /**
     * POST类型请求
     *
     * @param url:      请求路径
     * @param reqBody:  请求类型
     * @param respType: 返回体类型
     * @return: T
     **/
    public <T> T request(String url, Object reqBody, Class<T> respType) {
        Class api = reqBody.getClass();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            String data = JsonUtils.obj2String(reqBody);
            logger.info("====================> <POST> Request athenaGdb api:{},  data:{}", api, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reqBody));
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            HttpEntity<String> formEntity = new HttpEntity<>(data, headers);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            logger.info("====================> <POST> Request athenaGdb api:{},  request:{}", api, url);
            String responseBody = RestTemplateUtil.post(url, formEntity, String.class).getBody();
            stopWatch.stop();
            logger.info("====================> <POST> Request athenaGdb api:{},  use time:{}s", api, stopWatch.getTotalTimeSeconds());
            logger.info("====================> <POST> Rquest athenaGdb api:{},  response:{}", api,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(responseBody, new TypeReference<T>() {
                    })));

            return mapper.readValue(responseBody, new TypeReference<T>() {
            });
        } catch (Exception e) {
            logger.error(" <POST> Request exception athenaGdb api:{}, seq:{}", api, e);
            throw new RuntimeException(" <POST> Request athenaGdb exception");
        }
    }


    /**
     * Get类型的请求
     *
     * @param url:      请求路径
     * @param respType: 返回体类型
     * @return: T
     **/
    public <T> T request(String url, Class<T> respType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
            headers.setContentType(type);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            logger.info("====================> <GET> Request athenaGdb api:{},  request:{}", url);
            String responseBody = RestTemplateUtil.get(url, String.class).getBody();
            stopWatch.stop();
            logger.info("====================> <GET> Request athenaGdb ,  use time:{}s", stopWatch.getTotalTimeSeconds());
            logger.info("====================> <GET> Rquest athenaGdb url:{},  response:{}", url,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(responseBody, new TypeReference<T>() {
                    })));

            return mapper.readValue(responseBody, new TypeReference<T>() {
            });
        } catch (Exception e) {
            logger.error(" <GET> Request exception athenaGdb api:{}, seq:{}", url, e);
            throw new RuntimeException(" <GET> Request athenaGdb exception");
        }
    }
}