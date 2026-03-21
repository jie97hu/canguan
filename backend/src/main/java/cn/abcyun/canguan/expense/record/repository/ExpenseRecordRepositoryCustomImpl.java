package cn.abcyun.canguan.expense.record.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import cn.abcyun.canguan.expense.record.entity.ExpenseRecord;
import cn.abcyun.canguan.expense.record.support.ExpenseRecordFilter;
import cn.abcyun.canguan.expense.record.support.GroupAmountRow;
import cn.abcyun.canguan.expense.record.support.TrendGranularity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ExpenseRecordRepositoryCustomImpl implements ExpenseRecordRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ExpenseRecord> search(ExpenseRecordFilter filter, Pageable pageable) {
        StringBuilder sql = new StringBuilder("select r.* from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        sql.append(" order by r.expense_date desc, r.id desc");
        Query query = entityManager.createNativeQuery(sql.toString(), ExpenseRecord.class);
        applyParams(query, params);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        @SuppressWarnings("unchecked")
        List<ExpenseRecord> content = query.getResultList();
        long total = count(filter);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public BigDecimal sumAmount(ExpenseRecordFilter filter) {
        StringBuilder sql = new StringBuilder("select coalesce(sum(r.amount), 0) from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        Query query = entityManager.createNativeQuery(sql.toString());
        applyParams(query, params);
        Object result = query.getSingleResult();
        if (result instanceof BigDecimal) {
            return (BigDecimal) result;
        }
        if (result instanceof BigInteger) {
            return new BigDecimal((BigInteger) result);
        }
        return new BigDecimal(String.valueOf(result));
    }

    @Override
    public List<GroupAmountRow> groupByDate(ExpenseRecordFilter filter, TrendGranularity granularity) {
        String pattern = granularity == TrendGranularity.MONTH ? "%Y-%m" : "%Y-%m-%d";
        StringBuilder sql = new StringBuilder();
        sql.append("select date_format(r.expense_date, :pattern) as label, coalesce(sum(r.amount), 0) as amount, count(1) as cnt ");
        sql.append("from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        sql.append(" group by label order by label asc");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("pattern", pattern);
        applyParams(query, params);
        return mapGroupRows(query.getResultList(), true, false);
    }

    @Override
    public List<GroupAmountRow> groupByCategory1(ExpenseRecordFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append("select r.category_level1_id as id, max(r.category_level1_name) as name, coalesce(sum(r.amount), 0) as amount, count(1) as cnt ");
        sql.append("from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        sql.append(" group by r.category_level1_id order by amount desc, id asc");
        Query query = entityManager.createNativeQuery(sql.toString());
        applyParams(query, params);
        return mapGroupRows(query.getResultList(), false, true);
    }

    @Override
    public List<GroupAmountRow> groupByCategory2(ExpenseRecordFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append("select r.category_level2_id as id, max(r.category_level2_name) as name, coalesce(sum(r.amount), 0) as amount, count(1) as cnt ");
        sql.append("from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        sql.append(" group by r.category_level2_id order by amount desc, id asc");
        Query query = entityManager.createNativeQuery(sql.toString());
        applyParams(query, params);
        return mapGroupRows(query.getResultList(), false, true);
    }

    @Override
    public List<GroupAmountRow> groupByItemName(ExpenseRecordFilter filter, Integer topN) {
        StringBuilder sql = new StringBuilder();
        sql.append("select r.item_name as name, coalesce(sum(r.amount), 0) as amount, count(1) as cnt ");
        sql.append("from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        sql.append(" group by r.item_name order by amount desc, name asc");
        Query query = entityManager.createNativeQuery(sql.toString());
        applyParams(query, params);
        if (topN != null && topN > 0) {
            query.setMaxResults(topN);
        }
        return mapGroupRows(query.getResultList(), false, false);
    }

    @Override
    public List<GroupAmountRow> groupByStore(ExpenseRecordFilter filter) {
        StringBuilder sql = new StringBuilder();
        sql.append("select r.store_id as id, s.name as name, coalesce(sum(r.amount), 0) as amount, count(1) as cnt ");
        sql.append("from expense_record r join store s on s.id = r.store_id");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        sql.append(" group by r.store_id, s.name order by amount desc, id asc");
        Query query = entityManager.createNativeQuery(sql.toString());
        applyParams(query, params);
        return mapGroupRows(query.getResultList(), false, true);
    }

    private long count(ExpenseRecordFilter filter) {
        StringBuilder sql = new StringBuilder("select count(1) from expense_record r");
        Map<String, Object> params = new LinkedHashMap<>();
        appendWhere(sql, params, filter);
        Query query = entityManager.createNativeQuery(sql.toString());
        applyParams(query, params);
        Number result = (Number) query.getSingleResult();
        return result.longValue();
    }

    private void appendWhere(StringBuilder sql, Map<String, Object> params, ExpenseRecordFilter filter) {
        sql.append(" where r.deleted = :deleted");
        params.put("deleted", filter.isDeleted());
        if (filter.getStoreId() != null) {
            sql.append(" and r.store_id = :storeId");
            params.put("storeId", filter.getStoreId());
        }
        if (filter.getDateStart() != null) {
            sql.append(" and r.expense_date >= :dateStart");
            params.put("dateStart", java.sql.Date.valueOf(filter.getDateStart()));
        }
        if (filter.getDateEnd() != null) {
            sql.append(" and r.expense_date <= :dateEnd");
            params.put("dateEnd", java.sql.Date.valueOf(filter.getDateEnd()));
        }
        if (filter.getCategoryLevel1Id() != null) {
            sql.append(" and r.category_level1_id = :categoryLevel1Id");
            params.put("categoryLevel1Id", filter.getCategoryLevel1Id());
        }
        if (filter.getCategoryLevel2Id() != null) {
            sql.append(" and r.category_level2_id = :categoryLevel2Id");
            params.put("categoryLevel2Id", filter.getCategoryLevel2Id());
        }
        if (filter.getItemName() != null && !filter.getItemName().trim().isEmpty()) {
            sql.append(" and r.item_name like :itemName");
            params.put("itemName", "%" + filter.getItemName().trim() + "%");
        }
    }

    private void applyParams(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }

    private List<GroupAmountRow> mapGroupRows(List<?> rows, boolean hasLabel, boolean hasIdAndName) {
        List<GroupAmountRow> result = new ArrayList<>();
        for (Object row : rows) {
            Object[] values = (Object[]) row;
            GroupAmountRow item = new GroupAmountRow();
            int index = 0;
            if (hasLabel) {
                item.setLabel(values[index] == null ? null : String.valueOf(values[index]));
                index++;
            } else if (!hasIdAndName) {
                item.setName(values[index] == null ? null : String.valueOf(values[index]));
                index++;
            } else {
                item.setId(values[index] == null ? null : toLong(values[index]));
                index++;
                item.setName(values[index] == null ? null : String.valueOf(values[index]));
                index++;
            }
            if (!hasLabel && !hasIdAndName && values.length > 2) {
                item.setAmount(toBigDecimal(values[index]));
                index++;
            } else if (hasLabel) {
                item.setAmount(toBigDecimal(values[index]));
                index++;
            } else {
                item.setAmount(toBigDecimal(values[index]));
                index++;
            }
            if (values.length > index) {
                item.setCount(toLong(values[index]));
            }
            result.add(item);
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
