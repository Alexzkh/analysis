package com.zqykj;

import com.zqykj.core.aggregation.impl.AggregatedPageImpl;
import com.zqykj.domain.base.Festival;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 导入的节假日数据测试类
 * @Author zhangkehou
 * @Date 2022/1/11
 */
@SpringBootTest
@Slf4j
public class ImportFestivalDataTest {
    @Autowired
    private EntranceRepository entranceRepository;


    @Test
    public void testImportFestivalData() throws Exception {
        List<Festival> records = new ArrayList<>();

        String csvPath = "C:\\Users\\zhangkehou\\Desktop\\300-master\\节假日_202201111340.csv";
        String record;
        BufferedReader file = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvPath), "UTF-8"));

        // 设定UTF-8字符集，使用带缓冲区的字符输入流BufferedReader读取文件内容
//         logger.info("Reading file: {}", csvFilePath);
        file = new BufferedReader(new InputStreamReader(new FileInputStream(csvPath), "UTF-8"));
        //  file = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), "UTF-8"));
        file.readLine(); //跳过表头所在的行

        // 遍历数据行并存储在名为records的ArrayList中，每一行records中存储的对象为一个String数组
        while ((record = file.readLine()) != null) {
            String fields[] = record.split(",");
            Festival festival = new Festival();
            festival.setDateTime(fields[1].replace("\"", "").replace("\'", ""));
            festival.setFestival(fields[2].replace("\"", "").replace("\'", ""));
            records.add(festival);
        }

        //      entranceRepository.saveAll(records,"festival",Festival.class);

        AggregatedPageImpl<Festival> results = (AggregatedPageImpl<Festival>) entranceRepository.findAll("festival", Festival.class);
        List<Festival> re = results.getContent();
        String s = "2020-01-31";
        String s2 = "2004-01-02";

        boolean sRes = re.stream().anyMatch(m -> m.getDateTime().equals(s));
        boolean sRes2 = re.stream().anyMatch(m -> m.getDateTime().equals(s2));

        System.out.println(sRes);
        System.out.println(sRes2);
        System.out.println();
    }
}
