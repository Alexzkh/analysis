package com.zqykj.analysis;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.EnableESTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liuyi
 */
@SpringBootApplication
@EnableESTools(basePackages = "com.zqykj.tldw.aggregate.searching.esclientrhl")
public class AnalysisServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalysisServiceApplication.class, args);
    }

}
