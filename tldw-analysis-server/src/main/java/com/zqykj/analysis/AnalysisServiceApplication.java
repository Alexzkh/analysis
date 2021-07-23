package com.zqykj.analysis;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.EnableESTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author liuyi
 */
@SpringBootApplication
@EnableESTools("com.zqykj.analysis.entity")
@ComponentScan(value = "com.zqykj")
public class AnalysisServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalysisServiceApplication.class, args);
    }

}
