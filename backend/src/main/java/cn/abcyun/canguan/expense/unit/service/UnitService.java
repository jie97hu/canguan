package cn.abcyun.canguan.expense.unit.service;

import java.util.List;
import java.util.stream.Collectors;

import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.unit.dto.UnitOptionQueryRequest;
import cn.abcyun.canguan.expense.unit.entity.ExpenseUnit;
import cn.abcyun.canguan.expense.unit.repository.ExpenseUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final ExpenseUnitRepository expenseUnitRepository;

    @Transactional(readOnly = true)
    public List<String> listOptions(UnitOptionQueryRequest request) {
        int limit = request.getLimit() == null ? 200 : request.getLimit();
        List<ExpenseUnit> units = StringUtils.hasText(request.getKeyword())
                ? expenseUnitRepository.findByStatusAndNameContainingOrderBySortNoAscIdAsc(
                        StatusEnum.ENABLED,
                        request.getKeyword().trim(),
                        PageRequest.of(0, limit))
                : expenseUnitRepository.findByStatusOrderBySortNoAscIdAsc(
                        StatusEnum.ENABLED,
                        PageRequest.of(0, limit));
        return units.stream().map(ExpenseUnit::getName).collect(Collectors.toList());
    }

    @Transactional
    public String normalizeAndEnsure(String unitName) {
        if (!StringUtils.hasText(unitName)) {
            return null;
        }
        String normalized = unitName.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        // 候选单位与业务字段解耦，保存分类或支出时只补齐主数据，不反向修改历史记录。
        if (!expenseUnitRepository.findByName(normalized).isPresent()) {
            ExpenseUnit unit = new ExpenseUnit();
            unit.setName(normalized);
            unit.setSortNo(0);
            unit.setStatus(StatusEnum.ENABLED);
            try {
                expenseUnitRepository.save(unit);
            } catch (DataIntegrityViolationException ignored) {
                // 并发下如果已被其他请求写入，直接复用已存在的单位名称即可。
            }
        }
        return normalized;
    }
}
