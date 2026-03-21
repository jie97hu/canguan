package cn.abcyun.canguan.expense.user.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import cn.abcyun.canguan.expense.store.entity.Store;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.support.BusinessException;
import cn.abcyun.canguan.expense.support.CurrentUserContext;
import cn.abcyun.canguan.expense.support.CurrentUserProvider;
import cn.abcyun.canguan.expense.support.ErrorCode;
import cn.abcyun.canguan.expense.support.PageExtra;
import cn.abcyun.canguan.expense.support.PageResult;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import cn.abcyun.canguan.expense.user.dto.CurrentUserDto;
import cn.abcyun.canguan.expense.user.dto.UserDto;
import cn.abcyun.canguan.expense.user.dto.UserQueryRequest;
import cn.abcyun.canguan.expense.user.dto.UserUpsertRequest;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository sysUserRepository;
    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResult<UserDto> page(UserQueryRequest request) {
        ensureOwner();
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        Page<SysUser> page = sysUserRepository.findAll(buildSpec(request), pageable);
        return PageResult.of(page.map(this::toDto), PageExtra.of(null));
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        ensureOwner();
        return toDto(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CurrentUserDto current() {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        CurrentUserDto dto = new CurrentUserDto();
        dto.setId(currentUser.getId());
        dto.setUsername(currentUser.getUsername());
        dto.setDisplayName(currentUser.getDisplayName());
        dto.setRole(currentUser.getRole());
        dto.setStoreId(currentUser.getStoreId());
        dto.setStoreName(currentUser.getStoreName());
        dto.setStatus(currentUser.getStatus());
        return dto;
    }

    @Transactional
    public UserDto create(UserUpsertRequest request) {
        ensureOwner();
        validateUsernameUnique(request.getUsername(), null);
        SysUser user = new SysUser();
        fillUser(user, request, true);
        return toDto(sysUserRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UserUpsertRequest request) {
        ensureOwner();
        SysUser user = getEntity(id);
        validateUsernameUnique(request.getUsername(), id);
        fillUser(user, request, false);
        return toDto(sysUserRepository.save(user));
    }

    @Transactional
    public UserDto changeStatus(Long id, StatusEnum status) {
        ensureOwner();
        SysUser user = getEntity(id);
        user.setStatus(status);
        user.setTokenVersion(user.getTokenVersion() + 1);
        return toDto(sysUserRepository.save(user));
    }

    @Transactional
    public UserDto resetPassword(Long id, String password) {
        ensureOwner();
        SysUser user = getEntity(id);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return toDto(sysUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public SysUser requireEnabledUser(Long userId) {
        SysUser user = getEntity(userId);
        if (user.getStatus() == StatusEnum.DISABLED) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long userId) {
        return requireEnabledUser(userId).getRole() == UserRoleEnum.OWNER;
    }

    private void fillUser(SysUser user, UserUpsertRequest request, boolean isCreate) {
        user.setUsername(request.getUsername().trim());
        user.setDisplayName(request.getDisplayName().trim());
        user.setRole(request.getRole());
        user.setStatus(Optional.ofNullable(request.getStatus()).orElse(StatusEnum.ENABLED));
        if (request.getRole() == UserRoleEnum.CLERK) {
            if (request.getStoreId() == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "录入员必须绑定分店");
            }
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
            if (store.getStatus() == StatusEnum.DISABLED) {
                throw new BusinessException(ErrorCode.STORE_DISABLED);
            }
            user.setStoreId(store.getId());
        } else {
            user.setStoreId(request.getStoreId());
            if (request.getStoreId() != null) {
                Store store = storeRepository.findById(request.getStoreId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
                if (store.getStatus() == StatusEnum.DISABLED) {
                    throw new BusinessException(ErrorCode.STORE_DISABLED);
                }
            }
        }
        if (isCreate) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setTokenVersion(0L);
        } else if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setTokenVersion(user.getTokenVersion() + 1);
        }
    }

    private void ensureOwner() {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.getRole() != UserRoleEnum.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateUsernameUnique(String username, Long ignoreId) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "用户名不能为空");
        }
        String trimmed = username.trim();
        boolean exists = ignoreId == null ? sysUserRepository.existsByUsername(trimmed) : sysUserRepository.existsByUsernameAndIdNot(trimmed, ignoreId);
        if (exists) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "用户名已存在");
        }
    }

    private SysUser getEntity(Long id) {
        return sysUserRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private Specification<SysUser> buildSpec(UserQueryRequest request) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(request.getKeyword())) {
                String like = "%" + request.getKeyword().trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("displayName"), like)));
            }
            if (request.getRole() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("role"), request.getRole()));
            }
            if (request.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getStoreId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("storeId"), request.getStoreId()));
            }
            return predicate;
        };
    }

    private UserDto toDto(SysUser user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setRole(user.getRole());
        dto.setStoreId(user.getStoreId());
        if (user.getStoreId() != null) {
            storeRepository.findById(user.getStoreId()).ifPresent(store -> dto.setStoreName(store.getName()));
        }
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
