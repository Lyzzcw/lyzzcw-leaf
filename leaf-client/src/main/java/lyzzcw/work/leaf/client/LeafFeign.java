package lyzzcw.work.leaf.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lzy
 * @version 1.0
 * Date: 2024/11/20 15:15
 * Description: No Description
 */
@FeignClient(value = "leaf",path = "/api/snowflake"
//        ,fallback = LeafFallBackService.class
)
public interface LeafFeign {
    /**
     * 获取 Snowflake ID(serverId)
     *
     * @param key 钥匙
     * @return {@link String}
     */
    @GetMapping(value = "/get/{key}")
    String getSnowflakeId(@PathVariable("key") String key);


    /**
     * 获取 Snowflake ID
     *
     * @return {@link String}
     */
    @GetMapping(value = "/get")
    String getSnowflakeId();
}
