package cn.abcyun.canguan.expense.category.dto;

import java.util.ArrayList;
import java.util.List;

import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;

@Data
public class CategoryNodeDto {

    private Long id;

    private Long parentId;

    private Integer level;

    private String name;

    private String code;

    private String defaultUnit;

    private Integer sortNo;

    private StatusEnum status;

    private List<CategoryNodeDto> children = new ArrayList<>();
}
