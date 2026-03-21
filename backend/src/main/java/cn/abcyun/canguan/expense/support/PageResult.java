package cn.abcyun.canguan.expense.support;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list = Collections.emptyList();

    private long total;

    private int pageNo;

    private int pageSize;

    private PageExtra extra;

    public static <T> PageResult<T> of(Page<T> page, PageExtra extra) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), page.getNumber() + 1, page.getSize(), extra);
    }
}
