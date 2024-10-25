package lyzzcw.work.leaf.server.service;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.component.domain.common.entity.Result;
import lyzzcw.work.component.redis.semaphore.DistributedSemaphore;
import lyzzcw.work.component.redis.semaphore.factory.DistributedSemaphoreFactory;
import lyzzcw.work.leaf.core.IDGen;
import lyzzcw.work.leaf.core.exception.LeafException;
import lyzzcw.work.leaf.core.snowflake.CustomizeSnowflakeIDGenImpl;
import lyzzcw.work.leaf.core.snowflake.GeneralSnowflakeIDGenImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;


@Service("SnowflakeService")
@Slf4j
public class SnowflakeService {
    private final static String REDIS_LOCK_PREFIX = "SnowflakeService_Lock_";
    private IDGen idGen;

    // 使用 Environment 对象来获取
    private final Environment environment;
    private final DistributedSemaphoreFactory distributedSemaphoreFactory;

    @Autowired
    public SnowflakeService(Environment environment, DistributedSemaphoreFactory distributedSemaphoreFactory) throws LeafException {
        //redission 分布式锁 确认注册的顺序性
        this.environment = environment;
        this.distributedSemaphoreFactory = distributedSemaphoreFactory;
        DistributedSemaphore lock = distributedSemaphoreFactory.getDistributedSemaphore(REDIS_LOCK_PREFIX);
        String permitId = null;
        try {
            boolean permits = lock.trySetPermits(1);
            // 尝试获取一个许可，并指定这个许可的过期时间，单位是秒
            permitId = lock.tryAcquire(10, 30, TimeUnit.SECONDS);
            if(StringUtils.hasLength(permitId)){
                log.info("Redission Distributed Lock Init Successfully");
                String serverName = environment.getProperty("spring.application.name");
                String groupName = environment.getProperty("spring.cloud.nacos.discovery.group");
                String username = environment.getProperty("spring.cloud.nacos.discovery.username");
                String password = environment.getProperty("spring.cloud.nacos.discovery.password");
                int serverPort = Integer.parseInt(environment.getProperty("server.port"));
                String nacosAddr = environment.getProperty("spring.cloud.nacos.discovery.server-addr")
                        .concat("?namespace=")
                        .concat(environment.getProperty("spring.cloud.nacos.discovery.namespace"));
                String type = environment.getProperty("snowflake.type", "general");
                if(type.equals("general")){
                    idGen = new GeneralSnowflakeIDGenImpl(nacosAddr,username,password,serverName,groupName, serverPort);
                    log.info("General Snowflake IDGen Init Successfully");
                }else if(type.equals("customize")){
                    idGen = new CustomizeSnowflakeIDGenImpl(nacosAddr,username,password,serverName,groupName, serverPort);
                    log.info("Customize Snowflake IDGen Init Successfully");
                }else {
                    throw new LeafException("Snowflake type not found");
                }
                if (idGen.init()) {
                    log.info("Snowflake Service Init Successfully");
                } else {
                    throw new LeafException("Snowflake type not found");
                }
            }else {
                // 未获取到许可
                log.info("No permits available");
                throw new LeafException("Snowflake type not found");
            }
        }catch (Exception e){
            log.error("Snowflake Service Init Fail", e);
            throw new LeafException("Snowflake type not found");
        }finally {
            if(StringUtils.hasLength(permitId)){
                lock.release(permitId);
            }
        }

    }

    public Result getId(String key) {
        return idGen.get(key);
    }

    public Result getId(){
        return idGen.get();
    }
}
