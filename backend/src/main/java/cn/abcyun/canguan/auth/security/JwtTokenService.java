package cn.abcyun.canguan.auth.security;

import cn.abcyun.canguan.auth.config.JwtProperties;
import cn.abcyun.canguan.common.error.BaseErrorCode;
import cn.abcyun.canguan.common.error.BusinessException;
import cn.abcyun.canguan.common.model.CurrentUser;
import cn.abcyun.canguan.common.model.UserRole;
import cn.abcyun.canguan.common.model.UserStatus;
import cn.abcyun.canguan.common.util.IdUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtTokenService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_STORE_ID = "storeId";
    public static final String CLAIM_TOKEN_VERSION = "tokenVersion";
    public static final String CLAIM_SESSION_ID = "sid";
    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair createTokenPair(SecurityUserPrincipal principal, String sessionId) {
        Date now = new Date();
        Date accessExpireAt = new Date(now.getTime() + jwtProperties.getAccessTokenTtlSeconds() * 1000L);
        Date refreshExpireAt = new Date(now.getTime() + jwtProperties.getRefreshTokenTtlSeconds() * 1000L);
        String accessToken = buildToken(principal, sessionId, TOKEN_TYPE_ACCESS, now, accessExpireAt);
        String refreshToken = buildToken(principal, sessionId, TOKEN_TYPE_REFRESH, now, refreshExpireAt);
        return new TokenPair(accessToken, refreshToken, jwtProperties.getAccessTokenTtlSeconds(), sessionId, refreshExpireAt.toInstant());
    }

    public TokenClaims parseAccessToken(String token) {
        return parseToken(token, TOKEN_TYPE_ACCESS, BaseErrorCode.TOKEN_INVALID, BaseErrorCode.TOKEN_EXPIRED);
    }

    public TokenClaims parseRefreshToken(String token) {
        return parseToken(token, TOKEN_TYPE_REFRESH, BaseErrorCode.REFRESH_TOKEN_INVALID, BaseErrorCode.REFRESH_TOKEN_INVALID);
    }

    public CurrentUser toCurrentUser(TokenClaims claims) {
        return CurrentUser.builder()
                .id(claims.getUserId())
                .username(claims.getUsername())
                .displayName(claims.getDisplayName())
                .role(claims.getRole())
                .storeId(claims.getStoreId())
                .storeName(null)
                .status(UserStatus.ENABLED)
                .sessionId(claims.getSessionId())
                .build();
    }

    private TokenClaims parseToken(String token, String expectedType, BaseErrorCode invalidCode, BaseErrorCode expiredCode) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(jwtProperties.getClockSkewSeconds())
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseClaimsJws(token);
            Claims claims = jws.getBody();
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedType.equals(tokenType)) {
                throw new BusinessException(invalidCode);
            }
            return new TokenClaims(
                    claims.get(CLAIM_USER_ID, Number.class).longValue(),
                    claims.getSubject(),
                    claims.get("displayName", String.class),
                    UserRole.valueOf(claims.get(CLAIM_ROLE, String.class)),
                    claims.get(CLAIM_STORE_ID) == null ? null : claims.get(CLAIM_STORE_ID, Number.class).longValue(),
                    claims.get(CLAIM_TOKEN_VERSION) == null ? 0L : claims.get(CLAIM_TOKEN_VERSION, Number.class).longValue(),
                    claims.get(CLAIM_SESSION_ID, String.class),
                    tokenType,
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()
            );
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(expiredCode, ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(invalidCode, ex);
        }
    }

    private String buildToken(SecurityUserPrincipal principal, String sessionId, String tokenType, Date issuedAt, Date expireAt) {
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setIssuer(jwtProperties.getIssuer())
                .setSubject(principal.getUsername())
                .setId(IdUtils.newId())
                .setIssuedAt(issuedAt)
                .setExpiration(expireAt)
                .claim(CLAIM_USER_ID, principal.getId())
                .claim("displayName", principal.getDisplayName())
                .claim(CLAIM_ROLE, principal.getRole().name())
                .claim(CLAIM_TOKEN_VERSION, principal.getTokenVersion())
                .claim(CLAIM_SESSION_ID, sessionId)
                .claim(CLAIM_TOKEN_TYPE, tokenType);
        if (principal.getStoreId() != null) {
            builder.claim(CLAIM_STORE_ID, principal.getStoreId());
        }
        return builder.signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    @Value
    public static class TokenPair {
        String accessToken;
        String refreshToken;
        long expiresIn;
        String sessionId;
        Instant refreshExpiresAt;
    }

    @Value
    public static class TokenClaims {
        Long userId;
        String username;
        String displayName;
        UserRole role;
        Long storeId;
        Long tokenVersion;
        String sessionId;
        String tokenType;
        Instant issuedAt;
        Instant expiresAt;

        public LocalDateTime getExpiresAtDateTime() {
            return LocalDateTime.ofInstant(expiresAt, ZoneId.of("Asia/Shanghai"));
        }
    }
}
