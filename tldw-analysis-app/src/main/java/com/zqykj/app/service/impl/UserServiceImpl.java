package com.zqykj.app.service.impl;
import com.zqykj.app.service.IUserService;
import com.zqykj.domain.user.User;
import com.zqykj.infrastructure.constant.Constants;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.infrastructure.mapper.UserRowMapper;
import com.zqykj.tldw.aggregate.searching.hbaseclientrhl.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>创建用户服务实现</h1>
 * Created by Qinyi.
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    /** HBase 客户端 */
    private final HbaseTemplate hbaseTemplate;

    /** redis 客户端 */
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public UserServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    public ServerResponse createUser(User user) throws Exception {

        byte[] FAMILY_B = Constants.UserTable.FAMILY_B.getBytes();
        byte[] NAME = Constants.UserTable.NAME.getBytes();
        byte[] AGE = Constants.UserTable.AGE.getBytes();
        byte[] SEX = Constants.UserTable.SEX.getBytes();

        byte[] FAMILY_O = Constants.UserTable.FAMILY_O.getBytes();
        byte[] PHONE = Constants.UserTable.PHONE.getBytes();
        byte[] ADDRESS = Constants.UserTable.ADDRESS.getBytes();
       // redisTemplate.opsForValue().increment(Constants.USE_COUNT_REDIS_KEY, 1);

        Long curCount =1L;
        Long userId = genUserId(curCount);

        List<Mutation> datas = new ArrayList<Mutation>();
        Put put = new Put(Bytes.toBytes(userId));

        put.addColumn(FAMILY_B, NAME, Bytes.toBytes(user.getBaseInfo().getName()));
        put.addColumn(FAMILY_B, AGE, Bytes.toBytes(user.getBaseInfo().getAge()));
        put.addColumn(FAMILY_B, SEX, Bytes.toBytes(user.getBaseInfo().getSex()));

        put.addColumn(FAMILY_O, PHONE, Bytes.toBytes(user.getOtherInfo().getPhone()));
        put.addColumn(FAMILY_O, ADDRESS, Bytes.toBytes(user.getOtherInfo().getAddress()));

        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.UserTable.TABLE_NAME, datas);

        user.setId(userId);

        return ServerResponse.createBySuccess(user);
    }

    public ServerResponse find(Long userId) throws Exception {
//        byte[] reverseUserId = new StringBuilder(String.valueOf(userId)).reverse().toString().getBytes();
        Scan scan = new Scan();
        scan.setFilter(new PrefixFilter(Bytes.toBytes(userId)));
        List<User> users = hbaseTemplate.find(Constants.UserTable.TABLE_NAME, Constants.UserTable.FAMILY_B, new UserRowMapper());

        return  ServerResponse.createBySuccess(users);
    }

    /**
     * <h2>生成 userId</h2>
     * @param prefix 当前用户数
     * @return 用户 id
     * */
    private Long genUserId(Long prefix) {

        String suffix = RandomStringUtils.randomNumeric(5);
        return Long.valueOf(prefix + suffix);
    }
}
