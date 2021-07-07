package com.zqykj.analysis.controller;

import com.zqykj.analysis.entity.Main4;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.ElasticsearchTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyi
 * 测试用
 * @date 2021-06-15
 */
@RestController
public class HelloController {

    @Autowired
    private ElasticsearchTemplate<Main4,String> elasticsearchTemplate;

    @RequestMapping(value = "/hello")
    public String hello() throws Exception {
        Main4 main1 = new Main4();
        main1.setProposal_no("main11");
        main1.setAppli_code("123");
        main1.setAppli_name("spring");
        main1.setRisk_code("0501");
        main1.setSum_premium(100);
        elasticsearchTemplate.save(main1);
        return "HELLO";
    }
}
