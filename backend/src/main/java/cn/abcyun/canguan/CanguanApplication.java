package cn.abcyun.canguan;

import cn.abcyun.canguan.auth.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class})
public class CanguanApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanguanApplication.class, args);
    }
}
