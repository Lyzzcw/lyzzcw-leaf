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
    @RequestMapping(value = "decodeSnowflakeId")
    @ResponseBody
    public Map<String, String> decodeSnowflakeId(@RequestParam("snowflakeId") String snowflakeIdStr) {
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
