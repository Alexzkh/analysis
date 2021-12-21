package com.zqykj;

import com.fasterxml.jackson.databind.JsonNode;
import com.xkzhangsan.time.calculator.DateTimeCalculatorUtil;
import com.zqykj.app.service.factory.ITransactionPathQueryRequestFactory;
import com.zqykj.app.service.strategy.analysis.proxy.BaseAPIProxy;
import com.zqykj.common.enums.AmountOperationSymbol;
import com.zqykj.common.request.TransactionPathRequest;
import com.zqykj.common.vo.DateRangeRequest;
import com.zqykj.constant.Constants;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.vo.TransactionPathResultVO;
import com.zqykj.infrastructure.util.JacksonUtils;
import com.zqykj.infrastructure.util.JsonUtils;
import com.zqykj.infrastructure.util.RestTemplateUtil;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.infrastructure.util.hashing.LongHashFunction;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.codehaus.jackson.type.TypeReference;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description: 图路径分析测试类
 * @Author zhangkehou
 * @Date 2021/12/8
 */
@SpringBootTest
@Slf4j
public class GraphPathAnalysisTest {

    private final static String URI = "http://172.30.4.55:8089";


    @Autowired
    private BaseAPIProxy baseAPIProxy;
    @Autowired
    private ITransactionPathQueryRequestFactory iTransactionPathQueryRequestFactory;

    @Autowired
    private EntranceRepository entranceRepository;

    @Test
    public void accessGraphIdTest() {

        HttpHeaders requestHeaders = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        requestHeaders.setContentType(type);
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
        String addBatchEdgeUrl = URI.concat(String.format("/graph/%s/%s/graphByKey", "tldw", "743fe4350752417595a9ddcf80ec9784"));
        ResponseEntity<String> responseEntity = RestTemplateUtil.get(addBatchEdgeUrl, String.class);
        Map<String, Object> gdbResponse = baseAPIProxy.request(addBatchEdgeUrl, Map.class);
        String body = responseEntity.getBody();
        JsonNode node = JacksonUtils.toObj(body);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        try {
            map = mapper.readValue(body, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedHashMap linkedHashMap = (LinkedHashMap) map.get("data");
//        Assert.assertEquals("5389992752447324542",(long)linkedHashMap.get("graphId"));
//60138216660047600
        System.out.println();
        long graphId = (long) linkedHashMap.get("graphId");
        Map<String, String> requestParam = new HashMap<>();
        List<String> fromKeyId = new ArrayList<>();

        fromKeyId.add(String.valueOf(createKeyId("tldw", graphId, "bank_card", "bank_card~`#60138216660030800")));
        fromKeyId.add(String.valueOf(createKeyId("tldw", graphId, "bank_card", "bank_card~`#60138216660047600")));


        List<String> toKeyId = new ArrayList<>();

        toKeyId.add(String.valueOf(createKeyId("tldw", graphId, "bank_card", "bank_card~`#60138216660029413")));

        HttpEntity<String> addGraphRequestBody = new HttpEntity(JsonUtils.obj2String(PathRequest.builder().vfromVKeyId(fromKeyId).vtoVKeyId(toKeyId).depth(2).direction("both").elpTypeKeys(Arrays.asList("bank_card_trade")).build()), requestHeaders);
//        System.out.println(JsonUtils.obj2String(PathRequest.builder().vfromVKeyId(fromKeyId).vtoVKeyId(toKeyId).depth(1).elpTypeKeys(Arrays.asList("bank_card_trade").build()));

        // /graph/{gid}/allpaths
        String allPathUri = URI.concat(String.format("/graph/%s/allpaths", "-4520587401131998390"));

        ResponseEntity<String> responseEntity1 = RestTemplateUtil.post(allPathUri, addGraphRequestBody, String.class);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            resultMap = mapper.readValue(responseEntity1.getBody(), new TypeReference<HashMap<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        String caseId = "743fe4350752417595a9ddcf80ec9784";
        Map<String, Object> objectMap = baseAPIProxy.request(allPathUri, PathRequest.builder().vfromVKeyId(fromKeyId).vtoVKeyId(toKeyId).depth(2).direction("both").elpTypeKeys(Arrays.asList("bank_card_trade")).build(), Map.class);
        requestParam.put("graphId", "7746113266265363756");
        LinkedHashMap<String, Object> re = (LinkedHashMap) objectMap.get("data");
        TransactionPathRequest request = TransactionPathRequest.builder()
                .dateRange(new DateRangeRequest("", ""))
                .fund("0")
                .operator(AmountOperationSymbol.gte)
                .build();
        List<TransactionPathResultVO> results = new ArrayList<>();
        for (Map.Entry<String, Object> entry : re.entrySet()) {
            System.out.println("key:" + entry.getKey() + "   value:" + entry.getValue());
//            List<> second  = (List) entry.getValue();
            List<List<LinkedHashMap<String, Object>>> path = (List<List<LinkedHashMap<String, Object>>>) entry.getValue();

            path.stream().forEach(pat -> {

                List list = pat.stream().map(cu -> cu.get("DataRows")).collect(Collectors.toList());

                List<String> rowIdsResult = new ArrayList<>();
                list.stream().forEach(rowIds -> {
                    List<LinkedHashMap<String, Object>> row = (List<LinkedHashMap<String, Object>>) rowIds;
                    row.stream().forEach(rowi -> {
                        String rowid = String.valueOf((long) rowi.get("__DATAROWID"));
                        rowIdsResult.add(rowid);
                    });
                });
                LinkedHashMap<String, Object> firstNode = pat.stream().findFirst().orElse(pat.get(pat.size() - 1));
                LinkedHashMap<String, Object> lastNode = pat.stream().skip(pat.size() - 1).findFirst().orElse(null);
                List<LinkedHashMap<String, Object>> dataRows = (List<LinkedHashMap<String, Object>>) firstNode.get("DataRows");

                // 终点id 集合
                List<String> endRowIds = dataRows.stream().map(m -> String.valueOf((long) m.get("__DATAROWID"))).collect(Collectors.toList());

                System.out.println("**************");

                List<LinkedHashMap<String, Object>> startDataRows = (List<LinkedHashMap<String, Object>>) lastNode.get("DataRows");
                // 起始id 集合
                List<String> startRowIds = startDataRows.stream().map(m -> String.valueOf((long) m.get("__DATAROWID"))).collect(Collectors.toList());
                System.out.println("****************");

                //开始组装sql拼接查询

                //开始组装sql拼接查询
                QuerySpecialParams source = iTransactionPathQueryRequestFactory.accessTransactionPathDataByCondition(request, caseId, startRowIds);
                QuerySpecialParams dest = iTransactionPathQueryRequestFactory.accessTransactionPathDataByCondition(request, caseId, endRowIds);
                PageRequest sourcePageRequest = new PageRequest(0, 1, Sort.by(Sort.Direction.ASC, "trading_time"));

                PageRequest destPageRequest = new PageRequest(0, 1, Sort.by(Sort.Direction.DESC, "trading_time"));
                Page<BankTransactionFlow> sourceFlow = entranceRepository.findAll(sourcePageRequest, caseId, BankTransactionFlow.class, source);

                Page<BankTransactionFlow> destFlow = entranceRepository.findAll(destPageRequest, caseId, BankTransactionFlow.class, dest);
                TransactionPathResultVO transactionPathResultVO = TransactionPathResultVO.builder()
                        .destAccount(destFlow.getContent().get(0).getTransactionOppositeAccount())
                        .destTransactionTime(destFlow.getContent().get(0).getTradingTime())
                        .destIdentityCard(destFlow.getContent().get(0).getTransactionOppositeCertificateNumber())
                        .destName(destFlow.getContent().get(0).getTransactionOppositeName())
                        .destTransactionMoney(new BigDecimal(destFlow.getContent().get(0).getTransactionMoney()))
                        .sourceAccount(sourceFlow.getContent().get(0).getQueryAccount())
                        .sourceIdentityCard(sourceFlow.getContent().get(0).getCustomerIdentityCard())
                        .sourceName(sourceFlow.getContent().get(0).getCustomerName())
                        .sourceTransactionMoney(new BigDecimal(sourceFlow.getContent().get(0).getTransactionMoney()))
                        .sourceTransactionTime(sourceFlow.getContent().get(0).getTradingTime())
                        .timeSpan(DateTimeCalculatorUtil.betweenTotalDays(sourceFlow.getContent().get(0).getTradingTime(), destFlow.getContent().get(0).getTradingTime()))
                        .ids(rowIdsResult)
                        .build();
                results.add(transactionPathResultVO);
            });


        }
        System.out.println("*****");

    }


    /**
     * 生成边Id，规则：哈希(fkeyid + "-" + tokeyid)
     *
     * @param fkeyid  起点keyId
     * @param tokeyid 终点点keyid
     * @return
     */
    public static Long edgeIdHashcode(long fkeyid, long tokeyid) {
        String str = fkeyid + "-" + tokeyid;
        return LongHashFunction.xx3().hashChars(str);
    }

    /**
     * 顶点keyId生成规则
     *
     * @param schema     图方案key
     * @param gid        图Id（caseID）
     * @param eletypeKey 实体对象Key
     * @param id         实体主键
     * @return
     */
    public static long createKeyId(String schema, long gid, String eletypeKey, String id) {
        return LongHashFunction.xx().hashChars(schema + "_" + gid + "_" + eletypeKey + "_" + id);
    }


    /**
     * To string from stream.
     *
     * @param input    stream
     * @param encoding charset of stream
     * @return string
     * @throws IOException io exception
     */
    public static String toString(InputStream input, String encoding) throws IOException {
        if (input == null) {
            return StringUtils.EMPTY;
        }
        return (null == encoding) ? toString(new InputStreamReader(input, Constants.ENCODE))
                : toString(new InputStreamReader(input, encoding));
    }

    /**
     * To string from reader.
     *
     * @param reader reader
     * @return string
     * @throws IOException io exception
     */
    public static String toString(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toString();
    }

    /**
     * Copy data.
     *
     * @param input  source
     * @param output target
     * @return copy size
     * @throws IOException io exception
     */
    public static long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n = 0; (n = input.read(buffer)) >= 0; ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    public static int findTargetSumWays(int[] nums, int target) {
        int length = nums.length;
        //求数组中所有数字的和
        int sum = 0;
        for (int num : nums)
            sum += num;
        //如果所有数字的和小于target，或者sum - target是奇数，
        //说明无论怎么添加符号，表达式的值都不可能是target
        if (sum < target || ((sum - target) & 1) != 0) {
            return 0;
        }
        //我们要找到一些元素让他们的和等于capacity的方案数即可。
        int capacity = (sum - target) >> 1;
        //dp[i][j]表示在数组nums的前i元素中选择一些元素，
        //使得选择的元素之和等于j的方案数
        int dp[][] = new int[length + 1][capacity + 1];
        //边界条件
        dp[0][0] = 1;
        for (int i = 1; i <= length; i++) {
            for (int j = 0; j <= capacity; j++) {
                //下面是递推公式
                if (j >= nums[i - 1]) {//不选第i个和选第i个元素
                    dp[i][j] = dp[i - 1][j] + dp[i - 1][j - nums[i - 1]];
                } else {//不能选择第i个元素
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        //从数组前length个（也就是全部）元素中选择一些元素，让他们的
        //和等于capacity的方案数。
        return dp[length][capacity];
    }

    public static void main(String[] args) {
        int[] nums = {7, 9, 8, 4, 1, 23, 4};
        int target = 18;
//        findTargetSumWays(nums,target);

        int capacity = (10 - 2) >> 1;
        System.out.println("***************" + capacity);

        int capacity1 = (2 - 2) >> 1;
        System.out.println("***************" + capacity1);

        int capacity2 = (7 - 2) >> 1;
        System.out.println("***************" + capacity2);

        int capacity3 = (10 - 2) >> 1;
        System.out.println("***************" + capacity3);

        int capacity4 = (45 - 2) >> 1;
        System.out.println("***************" + capacity4);

        int capacity5 = (8 - 2) << 3;
        System.out.println("***************" + capacity5);

    }


    /**
     * 位图排序
     */
    @Test
    public void testBitMapSort() {
        int[] array2 = new int[10];
        Random random = new Random();
        for (int i = 0; i < array2.length; i++) {
            array2[i] = random.nextInt(1000) - 500;

        }
        System.out.println("排序前：" + Arrays.toString(array2));
        int[] temp = bitMapSortTest(array2);
        System.out.println("排序后：" + Arrays.toString(temp));
    }


    public static int[] bitMapSortTest(int[] arr) {
        int[] value = getMaxArrayBitMap(arr);
        int N = (value[0] - value[1]) / 64 + 1;
        long[] bitMap = new long[N];
        for (int i = 0; i < arr.length; i++) {
            bitMap[(arr[i] - value[1]) / 64] |= 1L << ((arr[i] - value[1]) % 64);
        }
        int k = 0;
        int[] temp = new int[arr.length];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 64; j++) {
                if ((bitMap[i] & (1L << j)) != 0) {
                    temp[k++] = i * 64 + j + value[1];
                }

            }

        }
        if (k < arr.length) {
            return Arrays.copyOfRange(arr, 0, k);
        }
        return temp;

    }

    private static int[] getMaxArrayBitMap(int[] bitMap) {
        int max = bitMap[0];
        int min = bitMap[0];
        for (int i = 1; i < bitMap.length; i++) {
            if (max < bitMap[i]) {
                max = bitMap[i];
            } else if (min > bitMap[i]) {
                min = bitMap[i];
            }

        }
        return new int[]{max, min};

    }
}
