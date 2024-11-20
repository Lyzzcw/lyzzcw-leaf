package lyzzcw.work.leaf.demo;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.IDGen;
import lyzzcw.work.leaf.core.snowflake.LocalSnowflakeIDGenImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.stereotype.Component;

/**
 * @author lzy
 * @version 1.0
 * Date: 2024/11/20 15:26
 * Description: No Description
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "lyzzcw.work.leaf")
@Slf4j
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
