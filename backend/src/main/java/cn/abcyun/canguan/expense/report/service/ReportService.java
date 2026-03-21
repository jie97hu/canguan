package cn.abcyun.canguan.expense.report.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.abcyun.canguan.expense.record.entity.ExpenseRecord;
import cn.abcyun.canguan.expense.record.repository.ExpenseRecordRepository;
import cn.abcyun.canguan.expense.record.support.ExpenseRecordFilter;
import cn.abcyun.canguan.expense.record.support.GroupAmountRow;
import cn.abcyun.canguan.expense.record.support.TrendGranularity;
import cn.abcyun.canguan.expense.report.dto.CategoryBreakdownDto;
import cn.abcyun.canguan.expense.report.dto.CategoryBreakdownQueryRequest;
import cn.abcyun.canguan.expense.report.dto.ItemRankingDto;
import cn.abcyun.canguan.expense.report.dto.ItemRankingQueryRequest;
import cn.abcyun.canguan.expense.report.dto.OverviewDto;
import cn.abcyun.canguan.expense.report.dto.OverviewQueryRequest;
import cn.abcyun.canguan.expense.report.dto.StoreComparisonDto;
import cn.abcyun.canguan.expense.report.dto.StoreComparisonQueryRequest;
import cn.abcyun.canguan.expense.report.dto.TrendPointDto;
import cn.abcyun.canguan.expense.report.dto.TrendQueryRequest;
import cn.abcyun.canguan.expense.store.entity.Store;
import cn.abcyun.canguan.expense.store.repository.StoreRepository;
import cn.abcyun.canguan.expense.support.BusinessException;
import cn.abcyun.canguan.expense.support.CurrentUserContext;
import cn.abcyun.canguan.expense.support.CurrentUserProvider;
import cn.abcyun.canguan.expense.support.ErrorCode;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRecordRepository expenseRecordRepository;
    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public OverviewDto overview(OverviewQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecordFilter rangeFilter = buildFilter(request, currentUser, true);
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        BigDecimal todayAmount = expenseRecordRepository.sumAmount(buildFilter(copyWithDateRange(request, today, today), currentUser, true));
        BigDecimal monthAmount = expenseRecordRepository.sumAmount(buildFilter(copyWithDateRange(request, monthStart, today), currentUser, true));
        BigDecimal rangeAmount = expenseRecordRepository.sumAmount(rangeFilter);
        List<GroupAmountRow> storeRows = expenseRecordRepository.groupByStore(rangeFilter);
        List<Store> stores = storeRepository.findAllByStatusOrderByIdAsc(StatusEnum.ENABLED);
        long storeCount = currentUser.getRole() == UserRoleEnum.OWNER ? stores.size() : (currentUser.getStoreId() == null ? 0 : 1);

        OverviewDto dto = new OverviewDto();
        dto.setTodayAmount(todayAmount);
        dto.setMonthAmount(monthAmount);
        dto.setRangeAmount(rangeAmount);
        dto.setStoreCount(storeCount);
        if (!storeRows.isEmpty()) {
            GroupAmountRow top = storeRows.get(0);
            dto.setTopStoreName(top.getName());
            dto.setTopStoreAmount(top.getAmount());
        } else {
            dto.setTopStoreName(null);
            dto.setTopStoreAmount(BigDecimal.ZERO);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<TrendPointDto> trend(TrendQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecordFilter filter = buildFilter(request, currentUser, true);
        TrendGranularity granularity = resolveGranularity(request.getDateStart(), request.getDateEnd());
        List<GroupAmountRow> rows = expenseRecordRepository.groupByDate(filter, granularity);
        return rows.stream().map(row -> {
            TrendPointDto dto = new TrendPointDto();
            dto.setLabel(row.getLabel());
            dto.setAmount(row.getAmount());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryBreakdownDto> categoryBreakdown(CategoryBreakdownQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecordFilter filter = buildFilter(request, currentUser, true);
        List<GroupAmountRow> rows = request.getCategoryLevel() != null && request.getCategoryLevel() == 2
                ? expenseRecordRepository.groupByCategory2(filter)
                : expenseRecordRepository.groupByCategory1(filter);
        BigDecimal total = expenseRecordRepository.sumAmount(filter);
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            total = BigDecimal.ZERO;
        }
        BigDecimal finalTotal = total;
        List<CategoryBreakdownDto> result = new ArrayList<>();
        for (GroupAmountRow row : rows) {
            CategoryBreakdownDto dto = new CategoryBreakdownDto();
            dto.setCategoryId(row.getId());
            dto.setCategoryName(row.getName());
            dto.setAmount(row.getAmount());
            dto.setRatio(finalTotal.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : row.getAmount().divide(finalTotal, 4, RoundingMode.HALF_UP));
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ItemRankingDto> itemRanking(ItemRankingQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecordFilter filter = buildFilter(request, currentUser, true);
        List<GroupAmountRow> rows = expenseRecordRepository.groupByItemName(filter, request.getTopN() == null ? 10 : request.getTopN());
        List<ItemRankingDto> result = new ArrayList<>();
        long rank = 1;
        for (GroupAmountRow row : rows) {
            ItemRankingDto dto = new ItemRankingDto();
            dto.setItemName(row.getName());
            dto.setAmount(row.getAmount());
            dto.setRankNo(rank++);
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<StoreComparisonDto> storeComparison(StoreComparisonQueryRequest request) {
        CurrentUserContext currentUser = currentUserProvider.requireCurrentUser();
        ExpenseRecordFilter filter = buildFilter(request, currentUser, false);
        List<GroupAmountRow> rows = expenseRecordRepository.groupByStore(filter);
        Map<Long, String> storeNameMap = loadStoreNameMap(rows.stream().map(GroupAmountRow::getId).collect(Collectors.toList()));
        List<StoreComparisonDto> result = new ArrayList<>();
        long rank = 1;
        for (GroupAmountRow row : rows) {
            StoreComparisonDto dto = new StoreComparisonDto();
            dto.setStoreId(row.getId());
            dto.setStoreName(StringUtils.hasText(row.getName()) ? row.getName() : storeNameMap.get(row.getId()));
            dto.setAmount(row.getAmount());
            dto.setRankNo(rank++);
            result.add(dto);
        }
        return result;
    }

    private ExpenseRecordFilter buildFilter(OverviewQueryRequest request, CurrentUserContext currentUser, boolean allowStoreFilter) {
        ExpenseRecordFilter filter = new ExpenseRecordFilter();
        filter.setStoreId(resolveStoreId(request.getStoreId(), currentUser, allowStoreFilter));
        filter.setDateStart(resolveStartDate(request.getDateStart()));
        filter.setDateEnd(resolveEndDate(request.getDateEnd()));
        filter.setCategoryLevel1Id(request.getCategoryLevel1Id());
        filter.setCategoryLevel2Id(request.getCategoryLevel2Id());
        filter.setDeleted(false);
        return filter;
    }

    private ExpenseRecordFilter buildFilter(StoreComparisonQueryRequest request, CurrentUserContext currentUser, boolean allowStoreFilter) {
        ExpenseRecordFilter filter = new ExpenseRecordFilter();
        filter.setStoreId(resolveStoreId(null, currentUser, allowStoreFilter));
        filter.setDateStart(resolveStartDate(request.getDateStart()));
        filter.setDateEnd(resolveEndDate(request.getDateEnd()));
        filter.setCategoryLevel1Id(request.getCategoryLevel1Id());
        filter.setCategoryLevel2Id(request.getCategoryLevel2Id());
        filter.setDeleted(false);
        return filter;
    }

    private Long resolveStoreId(Long requestStoreId, CurrentUserContext currentUser, boolean allowStoreFilter) {
        if (currentUser.getRole() == UserRoleEnum.CLERK) {
            if (currentUser.getStoreId() == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "录入员未绑定分店");
            }
            return currentUser.getStoreId();
        }
        if (!allowStoreFilter) {
            return requestStoreId;
        }
        return requestStoreId;
    }

    private OverviewQueryRequest copyWithDateRange(OverviewQueryRequest request, LocalDate dateStart, LocalDate dateEnd) {
        OverviewQueryRequest copy = new OverviewQueryRequest();
        copy.setStoreId(request.getStoreId());
        copy.setDateStart(dateStart);
        copy.setDateEnd(dateEnd);
        copy.setCategoryLevel1Id(request.getCategoryLevel1Id());
        copy.setCategoryLevel2Id(request.getCategoryLevel2Id());
        return copy;
    }

    private LocalDate resolveStartDate(LocalDate requestDate) {
        return requestDate;
    }

    private LocalDate resolveEndDate(LocalDate requestDate) {
        return requestDate;
    }

    private TrendGranularity resolveGranularity(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return TrendGranularity.DAY;
        }
        long days = ChronoUnit.DAYS.between(start, end);
        return days > 60 ? TrendGranularity.MONTH : TrendGranularity.DAY;
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (Store store : storeRepository.findAllById(storeIds)) {
            result.put(store.getId(), store.getName());
        }
        return result;
    }
}
