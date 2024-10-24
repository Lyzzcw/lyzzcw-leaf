package lyzzcw.work.leaf.core.snowflake;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.common.NacosUrlHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SnowflakeNacosHolder {
    public static final int NACOS_MAX_WORKER_ID = 1000;
    public static final String NACOS_WORKER_ID_KEY = "workerId";
    private String SERVER_NAME;
    private String GROUP_NAME;
    private int workerID = -1;

    private String ip;
    private String port;
    private String nacosAddr;
    private String username;
    private String password;
    private NamingService namingService;

    public SnowflakeNacosHolder(String ip, String port, String nacosAddr,String username, String password,
                                String serverName,String groupName) {
        this.ip = ip;
        this.port = port;
        this.nacosAddr = nacosAddr;
        this.username = username;
        this.password = password;
        this.SERVER_NAME = serverName;
        this.GROUP_NAME = groupName;
    }

    public boolean init() {
        try {
            this.namingService = NamingFactory.createNamingService(
                    NacosUrlHelper.url2Properties(nacosAddr,username,password));
            //获取实例列表
            List<Instance> idInstances = namingService.getAllInstances(SERVER_NAME, GROUP_NAME);
            int tempId = generateWorkerID(idInstances);

            //手动注册实例
            Instance instance = new Instance();
            instance.setIp(ip);
            instance.setPort(Integer.parseInt(port));
            // 2.3.1 版本后支持雪花id生成instanceId
            // 必须设置ephemeral=false，来保证服务端使用的是严格的一致性协议，否则可能会导致生成的instance id冲突：
//            instance.setEphemeral(false);//false为持久性节点 true为临时节点
            instance.setMetadata(new HashMap<String, String>());
            //这两个常量类由nacos提供 不需要自己动手写
            instance.getMetadata().put(PreservedMetadataKeys.INSTANCE_ID_GENERATOR, Constants.SNOWFLAKE_INSTANCE_ID_GENERATOR);
            instance.getMetadata().put(NACOS_WORKER_ID_KEY,tempId+"");

            namingService.registerInstance(SERVER_NAME, GROUP_NAME, instance);
            this.workerID = tempId;
        } catch (Exception e) {
            log.error("Start node ERROR", e);
            return false;
        }
        return true;
    }


    public void shutdown() {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(Integer.parseInt(port));
        try {
            namingService.deregisterInstance(SERVER_NAME, GROUP_NAME, instance);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getWorkerID() {
        return workerID;
    }

    public void setWorkerID(int workerID) {
        this.workerID = workerID;
    }

    /**
     * 生成 worker ID
     * workerId 的取值范围是 0-1023 这里做1000取余，1000-1023做本地workerId
     * @param instances 实例
     * @return int
     */
    private int generateWorkerID(List<Instance> instances) {
        log.debug("nacos instances: {}", instances);
        // 提取已经使用的 workerId
        Set<Integer> usedWorkerIds = new HashSet<>();
        for (Instance instance : instances) {
            if (instance.getMetadata().containsKey(NACOS_WORKER_ID_KEY)) {
                usedWorkerIds.add(Integer.parseInt(instance.getMetadata().get(NACOS_WORKER_ID_KEY)));
            }
        }

        // 分配一个新的 workerId，并确保它在 0 到 1023 范围内
        int tempId = 0;
        for (int i = 0; i <= NACOS_MAX_WORKER_ID; i++) {
            tempId = i % (NACOS_MAX_WORKER_ID + 1); // 确保 workerId 在 0 到 1023 范围内
            if (!usedWorkerIds.contains(tempId)) {
                break;
            }
        }

        // 如果所有 workerId 都被占用了，可以抛出异常或使用其他处理逻辑
        if (usedWorkerIds.contains(tempId)) {
            throw new RuntimeException("No available workerId found in the range 0-1023.");
        }

        return tempId;
    }


}
