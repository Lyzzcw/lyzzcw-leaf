package lyzzcw.work.leaf.core.snowflake;


import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.component.domain.common.entity.Result;
import lyzzcw.work.component.domain.common.exception.BaseException;
import lyzzcw.work.leaf.core.IDGen;
import lyzzcw.work.leaf.core.common.Status;

import java.util.Random;

/**
 * 本地 Snowflake idgen impl
 * 本地引用core依赖.通过启动参数配置workerId,1000-1023 防止leaf集群不可用
 * 启动参数增加 -DworkerId=1001
 * @author lzy
 * @date 2024/10/23
 */
@Slf4j
public class LocalSnowflakeIDGenImpl implements IDGen {

    @Override
    public boolean init() {
        return true;
    }

    private final long twepoch;
    private final long workerIdBits = 10L;
    private final long maxWorkerId = ~(-1L << workerIdBits);//最大能够分配的workerid =1023
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();

    public LocalSnowflakeIDGenImpl() {
        //Thu Nov 04 2010 09:42:54 GMT+0800 (中国标准时间)
        this(1288834974657L);
    }


    /**
     * 本地 Snowflake idgen impl
     *
     * @param twepoch 特沃波奇
     */
    public LocalSnowflakeIDGenImpl(long twepoch) {
        this.twepoch = twepoch;
        Preconditions.checkArgument(timeGen() > twepoch, "Snowflake not support twepoch gt currentTime");
        //java -DworkerId=1001 Main
        workerId = Long.parseLong(System.getProperty("workerId"));
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
        log.info("local general snowflake idgen init success workerId:{}", workerId);
    }

    @Override
    public long get(String key) {
        log.error("general snowflake idgen not support get serverId");
        throw new BaseException(Status.ERR_1002);
    }

    @Override
    public synchronized long get() {
        long timestamp = timeGen();
        //发生了回拨，此刻时间小于上次发号时间
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    //时间偏差大小小于5ms，则等待两倍时间
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        //还是小于,自动摘除本身节点并报警
                        throw new BaseException(Status.ERR_1001);
                    }
                } catch (InterruptedException e) {
                    log.error("wait interrupted");
                    throw new BaseException(Status.ERR_1001);
                }
            } else {
                //自动摘除本身节点并报警
                throw new BaseException(Status.ERR_1001);
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return id;

    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return workerId;
    }

}
