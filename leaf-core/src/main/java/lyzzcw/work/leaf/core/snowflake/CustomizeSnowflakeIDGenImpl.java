package lyzzcw.work.leaf.core.snowflake;


import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.IDGen;
import lyzzcw.work.leaf.core.common.Result;
import lyzzcw.work.leaf.core.common.Status;
import lyzzcw.work.leaf.core.common.Utils;

import java.util.Random;


/**
 * 自定义 Snowflake idgen impl
 * 增加区分业务服务位数标识
 * | 41 bits timestamp | 5 bits serviceId | 10 bits workerId | 7 bits sequence |
 * @author lzy
 * @date 2024/10/24
 */
@Slf4j
public class CustomizeSnowflakeIDGenImpl implements IDGen {

    @Override
    public boolean init() {
        return true;
    }
    //定义的纪元（Epoch），是一个固定的时间点，例如 2010-01-01。它用来减少时间戳的位数，避免存储非常大的时间戳。
    private final long twepoch;
    //workerId 占据 10 位（可支持最多 1024 个工作节点）
    private final long workerIdBits = 10L;
    private final long maxWorkerId = ~(-1L << workerIdBits);//最大能够分配的workerid =1023
    //服务标识占 5 位(可最多支持32个服务)
    private final long serviceIdBits = 5L;
    private final long maxServiceId = ~(-1L << serviceIdBits); // 最大 serviceId 值 31
    //序列号占用 7 位，这意味着在同一毫秒内，最多可以生成 128 个不同的 ID（0-127）
    private final long sequenceBits = 7L;
    private final long workerIdShift = sequenceBits;
    private final long serviceIdShift = sequenceBits + workerIdBits; // serviceId 左移 (7 + 10) = 17 位
    private final long timestampLeftShift = sequenceBits + workerIdBits + serviceIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();
    private static SnowflakeNacosHolder holder = null;

    public CustomizeSnowflakeIDGenImpl(String nacosAddr, String username, String password, String serverName, String groupName, int port) {
        //Thu Nov 04 2010 09:42:54 GMT+0800 (中国标准时间)
        this(nacosAddr,username,password,serverName,groupName, port, 1288834974657L);
    }


    /**
     * snowflake idgen impl
     *
     * @param nacosAddr  纳科斯 addr
     * @param serverName 服务器名称
     * @param groupName  组名称
     * @param port       港口
     * @param twepoch    特沃波奇
     */
    public CustomizeSnowflakeIDGenImpl(String nacosAddr, String username, String password, String serverName, String groupName, int port, long twepoch) {
        this.twepoch = twepoch;
        Preconditions.checkArgument(timeGen() > twepoch, "Snowflake not support twepoch gt currentTime");
        final String ip = Utils.getIp();
        holder = new SnowflakeNacosHolder(ip, String.valueOf(port), nacosAddr,username,password,serverName,groupName);
        log.info("twepoch:{} ,ip:{} ,nacosAddr:{} username:{} password:{} server:{} group:{} port:{}"
                , twepoch, ip, nacosAddr,username,password,serverName,groupName, port);
        boolean initFlag = holder.init();
        if (initFlag) {
            workerId = holder.getWorkerID();
            log.info("START SUCCESS USE NACOS WORKERID-{}", workerId);
        } else {
            Preconditions.checkArgument(initFlag, "Snowflake Id Gen is not init ok");
        }
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
    }

    @Override
    public Result get() {
        log.error("customize snowflake id gen get method is not support");
        return new Result(-4, Status.EXCEPTION);
    }

    @Override
    public synchronized Result get(String serviceKey) {
        long serviceId = Long.parseLong(serviceKey);
        Preconditions.checkArgument(serviceId >= 0 && serviceId <= maxServiceId, "serviceId must gte 0 and lte 31");
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
                        holder.shutdown();
                        return new Result(-1, Status.EXCEPTION);
                    }
                } catch (InterruptedException e) {
                    log.error("wait interrupted");
                    return new Result(-2, Status.EXCEPTION);
                }
            } else {
                //自动摘除本身节点并报警
                holder.shutdown();
                return new Result(-3, Status.EXCEPTION);
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
//                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
//            sequence = RANDOM.nextInt(100);
            sequence = 0;
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift)
                | (serviceId << serviceIdShift)
                | (workerId << workerIdShift)
                | sequence;
        return new Result(id, Status.SUCCESS);

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
