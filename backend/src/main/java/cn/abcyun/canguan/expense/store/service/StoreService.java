package cn.abcyun.canguan.expense.store.service;

import java.util.Optional;

import javax.persistence.criteria.Predicate;

import cn.abcyun.canguan.expense.store.dto.StoreDto;
import cn.abcyun.canguan.expense.store.dto.StoreQueryRequest;
import cn.abcyun.canguan.expense.store.dto.StoreUpsertRequest;
import cn.abcyun.canguan.expense.store.entity.Store;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.support.ApiResponse;
import cn.abcyun.canguan.expense.support.BusinessException;
import cn.abcyun.canguan.expense.support.CurrentUserContext;
import cn.abcyun.canguan.expense.support.CurrentUserProvider;
import cn.abcyun.canguan.expense.support.ErrorCode;
import cn.abcyun.canguan.expense.support.PageExtra;
import cn.abcyun.canguan.expense.support.PageResult;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public PageResult<StoreDto> page(StoreQueryRequest request) {
        ensureOwner();
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        Page<Store> page = storeRepository.findAll(buildSpec(request), pageable);
        return PageResult.of(page.map(this::toDto), PageExtra.of(null));
    }

    @Transactional(readOnly = true)
    public StoreDto get(Long id) {
        ensureOwner();
        return toDto(getEntity(id));
    }

    @Transactional(readOnly = true)
    public java.util.List<StoreDto> options() {
        ensureOwner();
        return storeRepository.findAllByStatusOrderByIdAsc(StatusEnum.ENABLED).stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public StoreDto create(StoreUpsertRequest request) {
        ensureOwner();
        validateCodeUnique(request.getCode(), null);
        Store store = new Store();
        store.setName(request.getName().trim());
        store.setCode(request.getCode().trim());
        store.setStatus(Optional.ofNullable(request.getStatus()).orElse(StatusEnum.ENABLED));
        return toDto(storeRepository.save(store));
    }

    @Transactional
    public StoreDto update(Long id, StoreUpsertRequest request) {
        ensureOwner();
        Store store = getEntity(id);
        validateCodeUnique(request.getCode(), id);
        store.setName(request.getName().trim());
        store.setCode(request.getCode().trim());
        store.setStatus(Optional.ofNullable(request.getStatus()).orElse(StatusEnum.ENABLED));
        return toDto(storeRepository.save(store));
    }

    @Transactional
    public StoreDto changeStatus(Long id, StatusEnum status) {
        ensureOwner();
        Store store = getEntity(id);
        store.setStatus(status);
        return toDto(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public Store requireActiveStore(Long storeId) {
        Store store = getEntity(storeId);
        if (store.getStatus() == StatusEnum.DISABLED) {
            throw new BusinessException(ErrorCode.STORE_DISABLED);
        }
        return store;
    }

    @Transactional(readOnly = true)
    public Store findById(Long id) {
        return getEntity(id);
    }

    private void ensureOwner() {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.getRole() != UserRoleEnum.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateCodeUnique(String code, Long ignoreId) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分店编码不能为空");
        }
        String trimmed = code.trim();
        boolean exists = ignoreId == null ? storeRepository.existsByCode(trimmed) : storeRepository.existsByCodeAndIdNot(trimmed, ignoreId);
        if (exists) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分店编码已存在");
        }
    }

    private Store getEntity(Long id) {
        return storeRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private Specification<Store> buildSpec(StoreQueryRequest request) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(request.getKeyword())) {
                String like = "%" + request.getKeyword().trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("code"), like)));
            }
            if (request.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), request.getStatus()));
            }
            return predicate;
        };
    }

    private StoreDto toDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setCode(store.getCode());
        dto.setStatus(store.getStatus());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        return dto;
    }
}
