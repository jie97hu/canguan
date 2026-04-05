package cn.abcyun.canguan.expense.category.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import cn.abcyun.canguan.expense.category.dto.CategoryNodeDto;
import cn.abcyun.canguan.expense.category.dto.CategoryQueryRequest;
import cn.abcyun.canguan.expense.category.dto.CategoryUpsertRequest;
import cn.abcyun.canguan.expense.category.entity.ExpenseCategory;
import cn.abcyun.canguan.expense.category.repository.ExpenseCategoryRepository;
import cn.abcyun.canguan.expense.store.service.StoreService;
import cn.abcyun.canguan.expense.support.BusinessException;
import cn.abcyun.canguan.expense.support.CurrentUserContext;
import cn.abcyun.canguan.expense.support.CurrentUserProvider;
import cn.abcyun.canguan.expense.support.ErrorCode;
import cn.abcyun.canguan.expense.support.PageExtra;
import cn.abcyun.canguan.expense.support.PageResult;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import cn.abcyun.canguan.expense.unit.service.UnitService;
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
public class CategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final CurrentUserProvider currentUserProvider;
    private final StoreService storeService;
    private final UnitService unitService;

    @Transactional(readOnly = true)
    public List<CategoryNodeDto> tree() {
        List<ExpenseCategory> all = expenseCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "sortNo").and(Sort.by(Sort.Direction.ASC, "id")));
        Map<Long, CategoryNodeDto> nodeMap = new HashMap<>();
        for (ExpenseCategory category : all) {
            nodeMap.put(category.getId(), toNode(category));
        }
        List<CategoryNodeDto> roots = new ArrayList<>();
        for (ExpenseCategory category : all) {
            CategoryNodeDto node = nodeMap.get(category.getId());
            if (category.getParentId() == null || category.getParentId() == 0L) {
                roots.add(node);
            } else {
                CategoryNodeDto parent = nodeMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        roots.sort(comparator());
        roots.forEach(this::sortChildrenRecursively);
        return roots;
    }

    @Transactional(readOnly = true)
    public PageResult<CategoryNodeDto> page(CategoryQueryRequest request) {
        ensureOwner();
        Pageable pageable = PageRequest.of(request.getPageNo() - 1, request.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        Page<ExpenseCategory> page = expenseCategoryRepository.findAll(buildSpec(request), pageable);
        return PageResult.of(page.map(this::toNode), PageExtra.of(null));
    }

    @Transactional(readOnly = true)
    public CategoryNodeDto get(Long id) {
        ensureOwner();
        return toNode(getEntity(id));
    }

    @Transactional
    public CategoryNodeDto create(CategoryUpsertRequest request) {
        ensureOwner();
        validateCodeUnique(request.getCode(), null);
        ExpenseCategory category = new ExpenseCategory();
        fillCategory(category, request);
        return toNode(expenseCategoryRepository.save(category));
    }

    @Transactional
    public CategoryNodeDto update(Long id, CategoryUpsertRequest request) {
        ensureOwner();
        ExpenseCategory category = getEntity(id);
        validateCodeUnique(request.getCode(), id);
        fillCategory(category, request);
        return toNode(expenseCategoryRepository.save(category));
    }

    @Transactional
    public CategoryNodeDto changeStatus(Long id, StatusEnum status) {
        ensureOwner();
        ExpenseCategory category = getEntity(id);
        category.setStatus(status);
        return toNode(expenseCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public ExpenseCategory requireActiveCategory(Long categoryId) {
        ExpenseCategory category = getEntity(categoryId);
        if (category.getStatus() == StatusEnum.DISABLED) {
            throw new BusinessException(ErrorCode.CATEGORY_DISABLED);
        }
        return category;
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategory> findActiveChildren(Long parentId) {
        return expenseCategoryRepository.findAllByParentIdOrderBySortNoAscIdAsc(parentId).stream()
                .filter(item -> item.getStatus() == StatusEnum.ENABLED)
                .collect(Collectors.toList());
    }

    private void fillCategory(ExpenseCategory category, CategoryUpsertRequest request) {
        if (request.getLevel() == null || (request.getLevel() != 1 && request.getLevel() != 2)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分类层级只能是1或2");
        }
        if (request.getLevel() == 1) {
            category.setParentId(null);
        } else {
            if (request.getParentId() == null || request.getParentId() <= 0) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "二级分类必须选择父分类");
            }
            ExpenseCategory parent = getEntity(request.getParentId());
            if (parent.getLevel() != 1) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "二级分类父节点必须是一级分类");
            }
            if (parent.getStatus() == StatusEnum.DISABLED) {
                throw new BusinessException(ErrorCode.CATEGORY_DISABLED);
            }
            category.setParentId(parent.getId());
        }
        category.setLevel(request.getLevel());
        category.setName(request.getName().trim());
        category.setCode(request.getCode().trim());
        // 分类默认单位继续存字符串，但候选来源改由单位主数据统一沉淀。
        category.setDefaultUnit(unitService.normalizeAndEnsure(request.getDefaultUnit()));
        category.setSortNo(Optional.ofNullable(request.getSortNo()).orElse(0));
        category.setStatus(Optional.ofNullable(request.getStatus()).orElse(StatusEnum.ENABLED));
    }

    private void validateCodeUnique(String code, Long ignoreId) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分类编码不能为空");
        }
        String trimmed = code.trim();
        boolean exists = ignoreId == null ? expenseCategoryRepository.existsByCode(trimmed) : expenseCategoryRepository.existsByCodeAndIdNot(trimmed, ignoreId);
        if (exists) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "分类编码已存在");
        }
    }

    private ExpenseCategory getEntity(Long id) {
        return expenseCategoryRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private void ensureOwner() {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.getRole() != UserRoleEnum.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Specification<ExpenseCategory> buildSpec(CategoryQueryRequest request) {
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
            if (request.getLevel() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("level"), request.getLevel()));
            }
            if (request.getParentId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("parentId"), request.getParentId()));
            }
            return predicate;
        };
    }

    private CategoryNodeDto toNode(ExpenseCategory category) {
        CategoryNodeDto dto = new CategoryNodeDto();
        dto.setId(category.getId());
        dto.setParentId(Optional.ofNullable(category.getParentId()).orElse(0L));
        dto.setLevel(category.getLevel());
        dto.setName(category.getName());
        dto.setCode(category.getCode());
        dto.setDefaultUnit(category.getDefaultUnit());
        dto.setSortNo(category.getSortNo());
        dto.setStatus(category.getStatus());
        return dto;
    }

    private void sortChildrenRecursively(CategoryNodeDto node) {
        node.getChildren().sort(comparator());
        node.getChildren().forEach(this::sortChildrenRecursively);
    }

    private Comparator<CategoryNodeDto> comparator() {
        return Comparator.comparing(CategoryNodeDto::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(CategoryNodeDto::getId, Comparator.nullsLast(Long::compareTo));
    }
}
