package com.zqykj.infrastructure.id;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * copy from http://www.cluozy.com/home/hexo/2018/08/11/shariding-JDBC-snowflake/.
 * description:分布式主键id生成器--基于雪花算法实现
 */
@SuppressWarnings("all")
public class SnowFlowerIdGenerator implements IdGenerator {

    /**
     * Start time intercept (2021-07-29 09:27)
     */
    public static final long EPOCH = 1627522024674L;


    private static final Logger logger = LoggerFactory.getLogger(SnowFlowerIdGenerator.class);

    /**
     * 序列所占位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * workerId所占位数
     */
    private static final long WORKER_ID_BITS = 10L;

    /**
     * 序列掩码（111111111111B = 4095）
     */
    private static final long SEQUENCE_MASK = 4095L;

    /**
     * workerId左边共12位（序列号）
     */
    private static final long WORKER_ID_LEFT_SHIFT_BITS = 12L;

    /**
     * 时间戳左边共22位（序列号+workerId）
     */
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = 22L;

    /**
     * 工作机器ID最大值1024
     */
    private static final long WORKER_ID_MAX_VALUE = 1024L;

    //
    /**
     * workId
     */
    private long workerId;

    /**
     * 序列号
     */
    private long sequence;

    /**
     * 最终时间
     */
    private long lastTime;

    /**
     * 当前ID
     */
    private long currentId;

    {
        long workerId = 1;

        if (workerId != -1) {
            this.workerId = workerId;
        } else {
            InetAddress address;
            try {
                address = InetAddress.getByName("172.30.6.31");
            } catch (final UnknownHostException e) {
                throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!", e);
            }
            byte[] ipAddressByteArray = address.getAddress();
            this.workerId = (((ipAddressByteArray[ipAddressByteArray.length - 2] & 0B11) << Byte.SIZE) + (
                    ipAddressByteArray[ipAddressByteArray.length - 1] & 0xFF));
        }
    }

    @Override
    public void init() {
        initialize(workerId);
    }

    @Override
    public long currentId() {
        return currentId;
    }

    @Override
    public synchronized long nextId() {
        long currentMillis = System.currentTimeMillis();
        Preconditions.checkState(this.lastTime <= currentMillis,
                "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds",
                new Object[]{this.lastTime, currentMillis});
        if (this.lastTime == currentMillis) {
            if (0L == (this.sequence = ++this.sequence & 4095L)) {
                currentMillis = this.waitUntilNextTime(currentMillis);
            }
        } else {
            this.sequence = 0L;
        }

        this.lastTime = currentMillis;
        logger.debug("{}-{}-{}", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(new Date(this.lastTime)),
                workerId, this.sequence);

        currentId = currentMillis - EPOCH << 22 | workerId << 12 | this.sequence;
        return currentId;
    }

    @Override
    public Map<Object, Object> info() {
        Map<Object, Object> info = new HashMap<>(4);
        info.put("currentId", currentId);
        info.put("workerId", workerId);
        return info;
    }

    // ==============================Constructors=====================================

    /**
     * init
     *
     * @param workerId worker id (0~1024)
     */
    public void initialize(long workerId) {
        if (workerId > WORKER_ID_MAX_VALUE || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0, current workId %d",
                            WORKER_ID_MAX_VALUE, workerId));
        }
        this.workerId = workerId;
    }

    /**
     * Block to the next millisecond until a new timestamp is obtained
     *
     * @param lastTimestamp The time intercept of the last ID generated
     * @return Current timestamp
     */
    private long waitUntilNextTime(long lastTimestamp) {
        long time;
        time = System.currentTimeMillis();
        while (time <= lastTimestamp) {
            ;
            time = System.currentTimeMillis();
        }

        return time;
    }

}
