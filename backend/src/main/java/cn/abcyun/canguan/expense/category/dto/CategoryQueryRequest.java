package cn.abcyun.canguan.expense.category.dto;

import cn.abcyun.canguan.expense.support.PageRequestDto;
import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryQueryRequest extends PageRequestDto {

    private String keyword;

    private StatusEnum status;

    private Integer level;

    private Long parentId;
}
