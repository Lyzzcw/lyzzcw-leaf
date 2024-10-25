package lyzzcw.work.leaf.server.controller;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.server.service.SnowflakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * id生成
 *
 * @author lzy
 * @date 2024/10/25
 */
@RestController
@Slf4j
public class LeafController {

    @Autowired
    private SnowflakeService snowflakeService;

    /**
     * 获取 Snowflake ID(serverId)
     *
     * @param key 钥匙
     * @return {@link String}
     */
    @RequestMapping(value = "/api/snowflake/get/{key}")
    public String getSnowflakeId(@PathVariable("key") String key) {
        return String.valueOf(snowflakeService.getId(key));
    }


    /**
     * 获取 Snowflake ID
     *
     * @return {@link String}
     */
    @RequestMapping(value = "/api/snowflake/get")
    public String getSnowflakeId() {
        return String.valueOf(snowflakeService.getId());
    }


}
