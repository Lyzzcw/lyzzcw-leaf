package lyzzcw.work.leaf.core.snowflake;


import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.IDGen;
import lyzzcw.work.leaf.core.common.Result;
import lyzzcw.work.leaf.core.common.Status;
import lyzzcw.work.leaf.core.common.Utils;

import java.util.Random;

@Slf4j
public class SnowflakeIDGenImpl implements IDGen {

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
    private static SnowflakeNacosHolder holder = null;

    public SnowflakeIDGenImpl(String nacosAddr,String username,String password,String serverName,String groupName, int port) {
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
    public SnowflakeIDGenImpl(String nacosAddr,String username,String password,String serverName,String groupName, int port, long twepoch) {
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
    public synchronized Result get(String key) {
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
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
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
