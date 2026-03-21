package cn.abcyun.canguan.auth.security;

import cn.abcyun.canguan.common.model.CurrentUser;
import cn.abcyun.canguan.common.model.UserRole;
import cn.abcyun.canguan.common.model.UserStatus;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String displayName;
    private UserRole role;
    private Long storeId;
    private String storeName;
    private UserStatus status;
    private String sessionId;
    private String passwordHash;
    private Long tokenVersion;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableList(AuthorityUtils.createAuthorityList("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ENABLED;
    }

    public CurrentUser toCurrentUser() {
        return CurrentUser.builder()
                .id(id)
                .username(username)
                .displayName(displayName)
                .role(role)
                .storeId(storeId)
                .storeName(storeName)
                .status(status)
                .sessionId(sessionId)
                .build();
    }

    public static SecurityUserPrincipal fromSysUser(SysUser user, String storeName) {
        return SecurityUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(UserRole.valueOf(user.getRole().name()))
                .storeId(user.getStoreId())
                .storeName(storeName)
                .status(UserStatus.valueOf(user.getStatus().name()))
                .passwordHash(user.getPasswordHash())
                .tokenVersion(user.getTokenVersion())
                .build();
    }
}
