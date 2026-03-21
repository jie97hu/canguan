package cn.abcyun.canguan.expense.record.repository;

import java.math.BigDecimal;
import java.util.List;

import cn.abcyun.canguan.expense.record.entity.ExpenseRecord;
import cn.abcyun.canguan.expense.record.support.ExpenseRecordFilter;
import cn.abcyun.canguan.expense.record.support.GroupAmountRow;
import cn.abcyun.canguan.expense.record.support.TrendGranularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseRecordRepositoryCustom {

    Page<ExpenseRecord> search(ExpenseRecordFilter filter, Pageable pageable);

    BigDecimal sumAmount(ExpenseRecordFilter filter);

    List<GroupAmountRow> groupByDate(ExpenseRecordFilter filter, TrendGranularity granularity);

    List<GroupAmountRow> groupByCategory1(ExpenseRecordFilter filter);

    List<GroupAmountRow> groupByCategory2(ExpenseRecordFilter filter);

    List<GroupAmountRow> groupByItemName(ExpenseRecordFilter filter, Integer topN);

    List<GroupAmountRow> groupByStore(ExpenseRecordFilter filter);
}
