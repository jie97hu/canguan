package cn.abcyun.canguan.expense.user.bootstrap;

import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OwnerBootstrapRunner {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${canguan.bootstrap.owner.username:}")
    private String username;

    @Value("${canguan.bootstrap.owner.password:}")
    private String password;

    @Value("${canguan.bootstrap.owner.display-name:}")
    private String displayName;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initOwner() {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(displayName)) {
            return;
        }
        if (sysUserRepository.countByRoleAndStatus(UserRoleEnum.OWNER, StatusEnum.ENABLED) > 0) {
            return;
        }
        SysUser user = new SysUser();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName.trim());
        user.setRole(UserRoleEnum.OWNER);
        user.setStatus(StatusEnum.ENABLED);
        user.setTokenVersion(0L);
        sysUserRepository.save(user);
    }
}
