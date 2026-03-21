package cn.abcyun.canguan.auth.service;

import cn.abcyun.canguan.auth.entity.RefreshTokenEntity;
import cn.abcyun.canguan.auth.repository.RefreshTokenRepository;
import cn.abcyun.canguan.auth.security.JwtTokenService;
import cn.abcyun.canguan.auth.security.SecurityUserPrincipal;
import cn.abcyun.canguan.auth.web.AuthWebModels;
import cn.abcyun.canguan.common.error.BaseErrorCode;
import cn.abcyun.canguan.common.error.BusinessException;
import cn.abcyun.canguan.common.util.IdUtils;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final SysUserRepository sysUserRepository;
    private final StoreRepository storeRepository;

    @Override
    public AuthWebModels.LoginResponse login(AuthWebModels.LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityUserPrincipal principal = (SecurityUserPrincipal) authentication.getPrincipal();
            String sessionId = IdUtils.newId();
            JwtTokenService.TokenPair tokenPair = jwtTokenService.createTokenPair(principal, sessionId);
            persistRefreshToken(principal, tokenPair);
            return buildLoginResponse(principal, tokenPair);
        } catch (DisabledException ex) {
            throw new BusinessException(BaseErrorCode.USER_DISABLED, ex);
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            throw new BusinessException(BaseErrorCode.UNAUTHORIZED, ex);
        }
    }

    @Override
    public AuthWebModels.LoginResponse refresh(AuthWebModels.RefreshRequest request) {
        JwtTokenService.TokenClaims claims = jwtTokenService.parseRefreshToken(request.getRefreshToken());
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByIdAndRevokedFalseAndExpiresAtAfter(
                claims.getSessionId(),
                LocalDateTime.now()
        ).orElseThrow(() -> new BusinessException(BaseErrorCode.REFRESH_TOKEN_INVALID));
        if (!refreshTokenEntity.getTokenHash().equals(hashToken(request.getRefreshToken()))) {
            throw new BusinessException(BaseErrorCode.REFRESH_TOKEN_INVALID);
        }
        refreshTokenEntity.setRevoked(Boolean.TRUE);
        refreshTokenRepository.save(refreshTokenEntity);

        SysUser user = sysUserRepository.findById(claims.getUserId())
                .orElseThrow(() -> new BusinessException(BaseErrorCode.UNAUTHORIZED));
        if (user.getStatus() == StatusEnum.DISABLED) {
            throw new BusinessException(BaseErrorCode.USER_DISABLED);
        }
        if (!user.getTokenVersion().equals(claims.getTokenVersion())) {
            throw new BusinessException(BaseErrorCode.REFRESH_TOKEN_INVALID);
        }
        SecurityUserPrincipal principal = SecurityUserPrincipal.fromSysUser(user, resolveStoreName(user));

        String newSessionId = IdUtils.newId();
        JwtTokenService.TokenPair tokenPair = jwtTokenService.createTokenPair(principal, newSessionId);
        persistRefreshToken(principal, tokenPair);
        return buildLoginResponse(principal, tokenPair);
    }

    @Override
    public void logout(AuthWebModels.LogoutRequest request) {
        JwtTokenService.TokenClaims claims = jwtTokenService.parseRefreshToken(request.getRefreshToken());
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByIdAndRevokedFalseAndExpiresAtAfter(
                claims.getSessionId(),
                LocalDateTime.now()
        ).orElseThrow(() -> new BusinessException(BaseErrorCode.REFRESH_TOKEN_INVALID));
        if (!refreshTokenEntity.getTokenHash().equals(hashToken(request.getRefreshToken()))) {
            throw new BusinessException(BaseErrorCode.REFRESH_TOKEN_INVALID);
        }
        refreshTokenEntity.setRevoked(Boolean.TRUE);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private void persistRefreshToken(SecurityUserPrincipal principal, JwtTokenService.TokenPair tokenPair) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(tokenPair.getSessionId());
        entity.setUserId(principal.getId());
        entity.setTokenHash(hashToken(tokenPair.getRefreshToken()));
        entity.setExpiresAt(LocalDateTime.ofInstant(tokenPair.getRefreshExpiresAt(), java.time.ZoneId.of("Asia/Shanghai")));
        entity.setRevoked(Boolean.FALSE);
        refreshTokenRepository.save(entity);
    }

    private AuthWebModels.LoginResponse buildLoginResponse(SecurityUserPrincipal principal, JwtTokenService.TokenPair tokenPair) {
        return new AuthWebModels.LoginResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                tokenPair.getExpiresIn(),
                AuthWebModels.CurrentUserResponse.from(principal.toCurrentUser())
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private String resolveStoreName(SysUser user) {
        if (user.getStoreId() == null) {
            return null;
        }
        return storeRepository.findById(user.getStoreId()).map(store -> store.getName()).orElse(null);
    }
}
