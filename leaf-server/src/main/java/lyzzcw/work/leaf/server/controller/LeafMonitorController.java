package lyzzcw.work.leaf.server.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.snowflake.SnowflakeNacosHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@Slf4j
public class LeafMonitorController {

    @Autowired
    private Environment env;

    /**
     * the output is like this:
     * {
     *   "timestamp": "1567733700834(2019-09-06 09:35:00.834)",
     *   "sequenceId": "3448",
     *   "workerId": "39"
     * }
     */
    @RequestMapping(value = "decodeGeneralId")
    @ResponseBody
    public Map<String, String> decodeGeneralId(@RequestParam("snowflakeId") String snowflakeIdStr) {
        Map<String, String> map = new HashMap<>();
        try {
            long snowflakeId = Long.parseLong(snowflakeIdStr);

            long originTimestamp = (snowflakeId >> 22) + 1288834974657L;
            Date date = new Date(originTimestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            map.put("timestamp", String.valueOf(originTimestamp) + "(" + sdf.format(date) + ")");

            long workerId = (snowflakeId >> 12) ^ (snowflakeId >> 22 << 10);
            map.put("workerId", String.valueOf(workerId));

            long sequence = snowflakeId ^ (snowflakeId >> 12 << 12);
            map.put("sequenceId", String.valueOf(sequence));
        } catch (NumberFormatException e) {
            map.put("errorMsg", "snowflake Id反解析发生异常!");
        }
        return map;
    }

    // 位移的定义
    private final long sequenceBits = 7L; // 序列号占7位
    private final long workerIdBits = 10L; // workerId占10位
    private final long serviceIdBits = 5L; // serviceId占5位
    private final long timestampBits = 41L; // 时间戳占41位

    // 各字段的左移位数
    private final long workerIdShift = sequenceBits; // workerId左移7位
    private final long serviceIdShift = sequenceBits + workerIdBits; // serviceId左移(7+10) = 17位
    private final long timestampLeftShift = sequenceBits + workerIdBits + serviceIdBits; // 时间戳左移(7+10+5) = 22位

    // 掩码定义
    private final long workerIdMask = -1L ^ (-1L << workerIdBits); // 用于提取10位workerId
    private final long serviceIdMask = -1L ^ (-1L << serviceIdBits); // 用于提取5位serviceId
    private final long sequenceMask = -1L ^ (-1L << sequenceBits); // 用于提取7位sequence

    /**
     * the output is like this:
     * {
     *   "timestamp": "1567733700834(2019-09-06 09:35:00.834)",
     *   "sequenceId": "3448",
     *   "workerId": "39",
     *   "serviceId": "20"
     * }
     */
    @RequestMapping(value = "decodeCustomizeId")
    @ResponseBody
    public Map<String, String> decodeCustomizeId(@RequestParam("snowflakeId") String snowflakeIdStr) {
        Map<String, String> map = new HashMap<>();
        try {
            long snowflakeId = Long.parseLong(snowflakeIdStr);

            long originTimestamp = (snowflakeId >> 22) + 1288834974657L;
            Date date = new Date(originTimestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            map.put("timestamp", String.valueOf(originTimestamp) + "(" + sdf.format(date) + ")");

            long serviceId = (snowflakeId >> 17) & serviceIdMask;
            map.put("serviceId", String.valueOf(serviceId));

            long workerId = (snowflakeId >> 7) & workerIdMask;
            map.put("workerId", String.valueOf(workerId));

            long sequence = snowflakeId & sequenceMask;
            map.put("sequenceId", String.valueOf(sequence));
        } catch (NumberFormatException e) {
            map.put("errorMsg", "snowflake Id反解析发生异常!");
        }
        return map;
    }


    @GetMapping(value = "getWorkerStatus")
    @ResponseBody
    public Map<String, String> getWorkerStatus() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", env.getProperty("spring.cloud.nacos.discovery.server-addr"));
        properties.put("namespace", env.getProperty("spring.cloud.nacos.discovery.namespace"));
        properties.put("username",env.getProperty("spring.cloud.nacos.discovery.username"));
        properties.put("password",env.getProperty("spring.cloud.nacos.discovery.password"));
        NamingService namingService = NamingFactory.createNamingService(properties);
        List<Instance> idInstances = namingService.getAllInstances(
                env.getProperty("spring.application.name"),
                env.getProperty("spring.cloud.nacos.discovery.group"));
        Map<String, String> map = new HashMap<>();
        idInstances.forEach(instance -> {
            String instanceKey = instance.getIp()
                    + ":" + instance.getPort()
                    + ":" + instance.getMetadata().get(SnowflakeNacosHolder.NACOS_WORKER_ID_KEY);
            String status = instance.isHealthy()? "UP" : "DOWN";
            map.put(instanceKey, status);
        });
        return map;
    }
}
