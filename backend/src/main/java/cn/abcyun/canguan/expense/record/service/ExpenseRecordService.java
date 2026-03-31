package cn.abcyun.canguan.expense.record.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.abcyun.canguan.expense.category.entity.ExpenseCategory;
import cn.abcyun.canguan.expense.category.service.CategoryService;
import cn.abcyun.canguan.expense.record.dto.ExpenseHistoryDto;
import cn.abcyun.canguan.expense.record.dto.ExpenseItemOptionQueryRequest;
import cn.abcyun.canguan.expense.record.dto.ExpenseQueryRequest;
import cn.abcyun.canguan.expense.record.dto.ExpenseRecordDto;
import cn.abcyun.canguan.expense.record.dto.ExpenseUpsertRequest;
import cn.abcyun.canguan.expense.record.entity.ExpenseRecord;
import cn.abcyun.canguan.expense.record.entity.ExpenseRecordHistory;
import cn.abcyun.canguan.expense.record.repository.ExpenseRecordHistoryRepository;
import cn.abcyun.canguan.expense.record.repository.ExpenseRecordRepository;
import cn.abcyun.canguan.expense.record.support.ExpenseRecordFilter;
import cn.abcyun.canguan.expense.record.support.GroupAmountRow;
import cn.abcyun.canguan.expense.store.entity.Store;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.store.service.StoreService;
import cn.abcyun.canguan.expense.support.BusinessException;
import cn.abcyun.canguan.expense.support.CurrentUserContext;
import cn.abcyun.canguan.expense.support.CurrentUserProvider;
import cn.abcyun.canguan.expense.support.ErrorCode;
import cn.abcyun.canguan.expense.support.PageExtra;
import cn.abcyun.canguan.expense.support.PageResult;
import cn.abcyun.canguan.expense.support.ExpenseActionEnum;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import cn.abcyun.canguan.expense.user.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ExpenseRecordService {

    private static final TypeReference<Map<String, Object>> SNAPSHOT_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final ExpenseRecordRepository expenseRecordRepository;
    private final ExpenseRecordHistoryRepository expenseRecordHistoryRepository;
    private final StoreRepository storeRepository;
    private final SysUserRepository sysUserRepository;
    private final StoreService storeService;
    private final CategoryService categoryService;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResult<ExpenseRecordDto> page(ExpenseQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecordFilter filter = toFilter(request, currentUser, true);
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<ExpenseRecord> page = expenseRecordRepository.search(filter, pageable);
        BigDecimal totalAmount = expenseRecordRepository.sumAmount(filter);
        Map<Long, Store> storeMap = loadStoreMap(page.getContent());
        Map<Long, SysUser> userMap = loadUserMap(page.getContent());
        return PageResult.of(page.map(record -> toDto(record, storeMap, userMap)), PageExtra.of(totalAmount));
    }

    @Transactional(readOnly = true)
    public ExpenseRecordDto get(Long id) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecord record = getVisibleRecord(id, currentUser);
        return toDto(record, loadStoreMap(java.util.Collections.singletonList(record)), loadUserMap(java.util.Collections.singletonList(record)));
    }

    @Transactional(readOnly = true)
    public List<String> listItemOptions(ExpenseItemOptionQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        Long storeId = resolveStoreIdForQuery(request.getStoreId(), currentUser, true);
        ExpenseCategory level1 = categoryService.requireActiveCategory(request.getCategoryLevel1Id());
        ExpenseCategory level2 = categoryService.requireActiveCategory(request.getCategoryLevel2Id());
        validateCategoryRelation(level1, level2);
        int limit = request.getLimit() == null ? 100 : request.getLimit();

        // 候选品项必须沿用现有数据隔离规则，避免录入员通过联想结果看到其他门店的名称。
        return expenseRecordRepository.findDistinctItemNames(
                storeId,
                level1.getId(),
                level2.getId(),
                PageRequest.of(0, limit)
        );
    }

    @Transactional
    public ExpenseRecordDto create(ExpenseUpsertRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        Long storeId = resolveStoreIdForWrite(request.getStoreId(), currentUser);
        Store store = storeService.requireActiveStore(storeId);
        ExpenseCategory level1 = categoryService.requireActiveCategory(request.getCategoryLevel1Id());
        ExpenseCategory level2 = categoryService.requireActiveCategory(request.getCategoryLevel2Id());
        validateCategoryRelation(level1, level2);
        ExpenseRecord record = new ExpenseRecord();
        fillRecord(record, request, currentUser, store.getId(), level1, level2);
        ExpenseRecord saved = expenseRecordRepository.save(record);
        saveHistory(saved.getId(), ExpenseActionEnum.CREATE, null, toSnapshot(saved), currentUser);
        return toDto(saved, loadStoreMap(java.util.Collections.singletonList(saved)), loadUserMap(java.util.Collections.singletonList(saved)));
    }

    @Transactional
    public ExpenseRecordDto update(Long id, ExpenseUpsertRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecord record = getVisibleRecord(id, currentUser);
        if (Boolean.TRUE.equals(record.getDeleted())) {
            throw new BusinessException(ErrorCode.EXPENSE_ALREADY_DELETED);
        }
        Map<String, Object> before = toSnapshot(record);
        Long storeId = resolveStoreIdForWrite(request.getStoreId(), currentUser);
        Store store = storeService.requireActiveStore(storeId);
        ExpenseCategory level1 = categoryService.requireActiveCategory(request.getCategoryLevel1Id());
        ExpenseCategory level2 = categoryService.requireActiveCategory(request.getCategoryLevel2Id());
        validateCategoryRelation(level1, level2);
        fillRecord(record, request, currentUser, store.getId(), level1, level2);
        ExpenseRecord saved = expenseRecordRepository.save(record);
        saveHistory(saved.getId(), ExpenseActionEnum.UPDATE, before, toSnapshot(saved), currentUser);
        return toDto(saved, loadStoreMap(java.util.Collections.singletonList(saved)), loadUserMap(java.util.Collections.singletonList(saved)));
    }

    @Transactional
    public void delete(Long id) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecord record = getVisibleRecord(id, currentUser);
        if (Boolean.TRUE.equals(record.getDeleted())) {
            throw new BusinessException(ErrorCode.EXPENSE_ALREADY_DELETED);
        }
        Map<String, Object> before = toSnapshot(record);
        record.setDeleted(Boolean.TRUE);
        record.setUpdatedBy(currentUser.getId());
        ExpenseRecord saved = expenseRecordRepository.save(record);
        saveHistory(saved.getId(), ExpenseActionEnum.DELETE, before, null, currentUser);
    }

    @Transactional(readOnly = true)
    public List<ExpenseHistoryDto> history(Long id) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecord record = getVisibleRecord(id, currentUser);
        List<ExpenseRecordHistory> histories = expenseRecordHistoryRepository.findByExpenseRecordIdOrderByOperateTimeDescIdDesc(record.getId());
        Map<Long, String> userNameMap = loadUserNameMap(histories.stream().map(ExpenseRecordHistory::getOperatorId).collect(Collectors.toList()));
        List<ExpenseHistoryDto> result = new ArrayList<>();
        for (ExpenseRecordHistory history : histories) {
            ExpenseHistoryDto dto = new ExpenseHistoryDto();
            dto.setId(history.getId());
            dto.setExpenseRecordId(history.getExpenseRecordId());
            dto.setAction(history.getAction());
            dto.setBeforeSnapshot(parseSnapshot(history.getBeforeSnapshot()));
            dto.setAfterSnapshot(parseSnapshot(history.getAfterSnapshot()));
            dto.setOperatorId(history.getOperatorId());
            dto.setOperatorName(userNameMap.get(history.getOperatorId()));
            dto.setOperateTime(history.getOperateTime());
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ExpenseRecord requireVisibleRecord(Long id, CurrentUserContext currentUser) {
        return getVisibleRecord(id, currentUser);
    }

    private ExpenseRecord getVisibleRecord(Long id, CurrentUserContext currentUser) {
        ExpenseRecord record = expenseRecordRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
        if (currentUser.getRole() == UserRoleEnum.CLERK && !currentUser.getStoreId().equals(record.getStoreId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return record;
    }

    private void fillRecord(ExpenseRecord record, ExpenseUpsertRequest request, CurrentUserContext currentUser, Long storeId,
                            ExpenseCategory level1, ExpenseCategory level2) {
        record.setStoreId(storeId);
        record.setExpenseDate(request.getExpenseDate());
        record.setCategoryLevel1Id(level1.getId());
        record.setCategoryLevel1Name(level1.getName());
        record.setCategoryLevel2Id(level2.getId());
        record.setCategoryLevel2Name(level2.getName());
        record.setItemName(request.getItemName().trim());
        record.setAmount(request.getAmount());
        record.setQuantity(request.getQuantity());
        record.setUnit(StringUtils.hasText(request.getUnit()) ? request.getUnit().trim() : null);
        record.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        if (record.getId() == null) {
            record.setCreatedBy(currentUser.getId());
        }
        record.setUpdatedBy(currentUser.getId());
        record.setDeleted(Boolean.FALSE);
    }

    private Long resolveStoreIdForWrite(Long requestStoreId, CurrentUserContext currentUser) {
        if (currentUser.getRole() == UserRoleEnum.CLERK) {
            if (currentUser.getStoreId() == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "录入员未绑定分店");
            }
            return currentUser.getStoreId();
        }
        if (requestStoreId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分店不能为空");
        }
        return requestStoreId;
    }

    private void validateCategoryRelation(ExpenseCategory level1, ExpenseCategory level2) {
        if (level1.getLevel() != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "一级分类必须选择一级节点");
        }
        if (level2.getLevel() != 2) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "二级分类必须选择二级节点");
        }
        if (!level1.getId().equals(level2.getParentId())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "二级分类必须属于所选一级分类");
        }
    }

    private ExpenseRecordFilter toFilter(ExpenseQueryRequest request, CurrentUserContext currentUser, boolean respectCurrentUserScope) {
        ExpenseRecordFilter filter = new ExpenseRecordFilter();
        filter.setStoreId(resolveStoreIdForQuery(request.getStoreId(), currentUser, respectCurrentUserScope));
        filter.setDateStart(request.getDateStart());
        filter.setDateEnd(request.getDateEnd());
        filter.setCategoryLevel1Id(request.getCategoryLevel1Id());
        filter.setCategoryLevel2Id(request.getCategoryLevel2Id());
        filter.setItemName(request.getItemName());
        filter.setDeleted(false);
        return filter;
    }

    private Long resolveStoreIdForQuery(Long requestStoreId, CurrentUserContext currentUser, boolean respectCurrentUserScope) {
        if (currentUser.getRole() == UserRoleEnum.CLERK) {
            if (currentUser.getStoreId() == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "录入员未绑定分店");
            }
            return currentUser.getStoreId();
        }
        if (!respectCurrentUserScope) {
            return requestStoreId;
        }
        return requestStoreId;
    }

    private Map<Long, Store> loadStoreMap(List<ExpenseRecord> records) {
        Set<Long> storeIds = records.stream().map(ExpenseRecord::getStoreId).collect(Collectors.toSet());
        if (storeIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, Store> result = new LinkedHashMap<>();
        for (Store store : storeRepository.findAllById(storeIds)) {
            result.put(store.getId(), store);
        }
        return result;
    }

    private Map<Long, SysUser> loadUserMap(List<ExpenseRecord> records) {
        Set<Long> userIds = records.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getCreatedBy(), item.getUpdatedBy()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, SysUser> result = new LinkedHashMap<>();
        for (SysUser user : sysUserRepository.findAllById(userIds)) {
            result.put(user.getId(), user);
        }
        return result;
    }

    private Map<Long, String> loadUserNameMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (SysUser user : sysUserRepository.findAllById(userIds)) {
            result.put(user.getId(), user.getDisplayName());
        }
        return result;
    }

    private ExpenseRecordDto toDto(ExpenseRecord record, Map<Long, Store> storeMap, Map<Long, SysUser> userMap) {
        ExpenseRecordDto dto = new ExpenseRecordDto();
        dto.setId(record.getId());
        dto.setStoreId(record.getStoreId());
        Store store = storeMap.get(record.getStoreId());
        if (store != null) {
            dto.setStoreName(store.getName());
        }
        dto.setExpenseDate(record.getExpenseDate());
        dto.setCategoryLevel1Id(record.getCategoryLevel1Id());
        dto.setCategoryLevel1Name(record.getCategoryLevel1Name());
        dto.setCategoryLevel2Id(record.getCategoryLevel2Id());
        dto.setCategoryLevel2Name(record.getCategoryLevel2Name());
        dto.setItemName(record.getItemName());
        dto.setAmount(record.getAmount());
        dto.setQuantity(record.getQuantity());
        dto.setUnit(record.getUnit());
        dto.setRemark(record.getRemark());
        dto.setCreatedBy(record.getCreatedBy());
        SysUser createdBy = userMap.get(record.getCreatedBy());
        if (createdBy != null) {
            dto.setCreatedByName(createdBy.getDisplayName());
        }
        dto.setUpdatedBy(record.getUpdatedBy());
        SysUser updatedBy = userMap.get(record.getUpdatedBy());
        if (updatedBy != null) {
            dto.setUpdatedByName(updatedBy.getDisplayName());
        }
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }

    private Map<String, Object> toSnapshot(ExpenseRecord record) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", record.getId());
        snapshot.put("storeId", record.getStoreId());
        snapshot.put("expenseDate", record.getExpenseDate());
        snapshot.put("categoryLevel1Id", record.getCategoryLevel1Id());
        snapshot.put("categoryLevel1Name", record.getCategoryLevel1Name());
        snapshot.put("categoryLevel2Id", record.getCategoryLevel2Id());
        snapshot.put("categoryLevel2Name", record.getCategoryLevel2Name());
        snapshot.put("itemName", record.getItemName());
        snapshot.put("amount", record.getAmount());
        snapshot.put("quantity", record.getQuantity());
        snapshot.put("unit", record.getUnit());
        snapshot.put("remark", record.getRemark());
        snapshot.put("createdBy", record.getCreatedBy());
        snapshot.put("updatedBy", record.getUpdatedBy());
        snapshot.put("deleted", record.getDeleted());
        return snapshot;
    }

    private void saveHistory(Long recordId, ExpenseActionEnum action, Map<String, Object> before, Map<String, Object> after, CurrentUserContext currentUser) {
        ExpenseRecordHistory history = new ExpenseRecordHistory();
        history.setExpenseRecordId(recordId);
        history.setAction(action);
        history.setBeforeSnapshot(writeSnapshot(before));
        history.setAfterSnapshot(writeSnapshot(after));
        history.setOperatorId(currentUser.getId());
        history.setOperatorName(currentUser.getDisplayName());
        history.setOperateTime(LocalDateTime.now(SHANGHAI));
        expenseRecordHistoryRepository.save(history);
    }

    private String writeSnapshot(Map<String, Object> snapshot) {
        if (snapshot == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, ex.getMessage());
        }
    }

    private Map<String, Object> parseSnapshot(String snapshot) {
        if (!StringUtils.hasText(snapshot)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(snapshot, SNAPSHOT_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, ex.getMessage());
        }
    }
}
