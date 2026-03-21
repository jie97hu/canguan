package cn.abcyun.canguan.auth.security;

import cn.abcyun.canguan.auth.repository.RefreshTokenRepository;
import cn.abcyun.canguan.common.api.ApiResponse;
import cn.abcyun.canguan.common.error.BaseErrorCode;
import cn.abcyun.canguan.common.error.BusinessException;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/webjars/**",
            "/actuator/health"
    );

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SysUserRepository sysUserRepository;
    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : SKIP_PATHS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring("Bearer ".length()).trim();
            JwtTokenService.TokenClaims claims = jwtTokenService.parseAccessToken(token);
            boolean sessionActive = refreshTokenRepository.findByIdAndRevokedFalseAndExpiresAtAfter(
                    claims.getSessionId(),
                    LocalDateTime.now()
            ).isPresent();
            if (!sessionActive) {
                throw new BusinessException(BaseErrorCode.TOKEN_INVALID);
            }

            SysUser user = sysUserRepository.findById(claims.getUserId())
                    .orElseThrow(() -> new BusinessException(BaseErrorCode.UNAUTHORIZED));
            if (user.getStatus() == StatusEnum.DISABLED) {
                throw new BusinessException(BaseErrorCode.USER_DISABLED);
            }
            if (!user.getTokenVersion().equals(claims.getTokenVersion())) {
                throw new BusinessException(BaseErrorCode.TOKEN_INVALID);
            }
            String storeName = null;
            if (user.getStoreId() != null) {
                storeName = storeRepository.findById(user.getStoreId()).map(store -> store.getName()).orElse(null);
            }
            SecurityUserPrincipal principal = SecurityUserPrincipal.fromSysUser(user, storeName);
            principal.setSessionId(claims.getSessionId());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.failure(ex.getErrorCode()));
        }
    }
}
