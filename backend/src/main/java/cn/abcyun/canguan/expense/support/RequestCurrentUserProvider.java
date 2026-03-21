package cn.abcyun.canguan.expense.support;

import javax.servlet.http.HttpServletRequest;

import cn.abcyun.canguan.auth.security.SecurityUserPrincipal;
import cn.abcyun.canguan.expense.store.entity.Store;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RequestCurrentUserProvider implements CurrentUserProvider {

    public static final String ATTRIBUTE_CURRENT_USER_CONTEXT = "CURRENT_USER_CONTEXT";
    public static final String ATTRIBUTE_CURRENT_USER_ID = "CURRENT_USER_ID";
    public static final String HEADER_CURRENT_USER_ID = "X-Current-User-Id";
    public static final String HEADER_USER_ID = "X-User-Id";

    private final HttpServletRequest request;
    private final SysUserRepository sysUserRepository;
    private final StoreRepository storeRepository;

    @Override
    public CurrentUserContext requireCurrentUser() {
        Object currentContext = request.getAttribute(ATTRIBUTE_CURRENT_USER_CONTEXT);
        if (currentContext instanceof CurrentUserContext) {
            return (CurrentUserContext) currentContext;
        }
        SecurityUserPrincipal principal = resolvePrincipal();
        if (principal != null) {
            CurrentUserContext context = new CurrentUserContext();
            context.setId(principal.getId());
            context.setUsername(principal.getUsername());
            context.setDisplayName(principal.getDisplayName());
            context.setRole(UserRoleEnum.valueOf(principal.getRole().name()));
            context.setStoreId(principal.getStoreId());
            context.setStoreName(principal.getStoreName());
            context.setStatus(StatusEnum.valueOf(principal.getStatus().name()));
            context.setTokenVersion(principal.getTokenVersion());
            request.setAttribute(ATTRIBUTE_CURRENT_USER_CONTEXT, context);
            request.setAttribute(ATTRIBUTE_CURRENT_USER_ID, principal.getId());
            return context;
        }
        Long currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        SysUser user = sysUserRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (user.getStatus() == StatusEnum.DISABLED) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        CurrentUserContext context = new CurrentUserContext();
        context.setId(user.getId());
        context.setUsername(user.getUsername());
        context.setDisplayName(user.getDisplayName());
        context.setRole(user.getRole());
        context.setStoreId(user.getStoreId());
        context.setStatus(user.getStatus());
        context.setTokenVersion(user.getTokenVersion());
        if (user.getStoreId() != null) {
            Store store = storeRepository.findById(user.getStoreId()).orElse(null);
            if (store != null) {
                context.setStoreName(store.getName());
            }
        }
        request.setAttribute(ATTRIBUTE_CURRENT_USER_CONTEXT, context);
        return context;
    }

    private SecurityUserPrincipal resolvePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUserPrincipal) {
            return (SecurityUserPrincipal) principal;
        }
        return null;
    }

    private Long resolveCurrentUserId() {
        Object attribute = request.getAttribute(ATTRIBUTE_CURRENT_USER_ID);
        if (attribute instanceof Long) {
            return (Long) attribute;
        }
        String rawId = request.getHeader(HEADER_CURRENT_USER_ID);
        if (!StringUtils.hasText(rawId)) {
            rawId = request.getHeader(HEADER_USER_ID);
        }
        if (!StringUtils.hasText(rawId)) {
            return null;
        }
        try {
            Long currentUserId = Long.valueOf(rawId.trim());
            request.setAttribute(ATTRIBUTE_CURRENT_USER_ID, currentUserId);
            return currentUserId;
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
