package lyzzcw.work.leaf.demo.config;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.IDGen;
import lyzzcw.work.leaf.core.snowflake.LocalSnowflakeIDGenImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author LZY
 * @version 1.0
 * Date: 2024/11/20 15:42
 * Description: No Description
 */
@Slf4j
@Configuration
public class GenConfiguration {

    @Bean
    public IDGen idGen() {
        return new LocalSnowflakeIDGenImpl();
    }

}
