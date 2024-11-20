package lyzzcw.work.leaf.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author lzy
 * @version 1.0
 * Date: 2024/11/20 15:26
 * Description: No Description
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "lyzzcw.work.leaf")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
