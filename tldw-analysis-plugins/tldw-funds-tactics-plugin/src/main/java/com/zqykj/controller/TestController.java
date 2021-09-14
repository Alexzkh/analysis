/**
 * @作者 Mcj
 */
package com.zqykj.controller;

import com.gitee.starblues.realize.PluginUtils;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping(path = "plugin2")
public class TestController {

    private final ElasticTestDao testDao;

    @Autowired
    public TestController(PluginUtils pluginUtils) {
        this.testDao = pluginUtils.getMainBean(ElasticTestDao.class);
    }

    @GetMapping("/test")
    public int test() throws Exception {
        return testDao.search(new MatchAllQueryBuilder()).size();
    }

}
