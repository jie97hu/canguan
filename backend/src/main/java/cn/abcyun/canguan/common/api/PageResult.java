package cn.abcyun.canguan.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list = Collections.emptyList();
    private long total;
    private int pageNo;
    private int pageSize;
    private Map<String, Object> extra = Collections.emptyMap();

    public static <T> PageResult<T> of(List<T> list, long total, int pageNo, int pageSize, Map<String, Object> extra) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        result.setExtra(extra);
        return result;
    }
}
