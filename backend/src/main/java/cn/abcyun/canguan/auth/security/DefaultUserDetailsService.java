package cn.abcyun.canguan.auth.security;

import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserDetailsService implements UserDetailsService {

    private final SysUserRepository sysUserRepository;
    private final StoreRepository storeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        String storeName = null;
        if (user.getStoreId() != null) {
            storeName = storeRepository.findById(user.getStoreId()).map(store -> store.getName()).orElse(null);
        }
        return SecurityUserPrincipal.fromSysUser(user, storeName);
    }
}
