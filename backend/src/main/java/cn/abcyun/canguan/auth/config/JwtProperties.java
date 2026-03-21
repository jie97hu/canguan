package cn.abcyun.canguan.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "canguan.jwt")
public class JwtProperties {

    private String issuer;
    private String secret;
    private long accessTokenTtlSeconds = 7200L;
    private long refreshTokenTtlSeconds = 604800L;
    private long clockSkewSeconds = 30L;
}
