package cn.abcyun.canguan.expense.category.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;

@Data
public class CategoryUpsertRequest {

    private Long parentId;

    @NotNull(message = "分类层级不能为空")
    private Integer level;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "分类编码不能为空")
    private String code;

    private String defaultUnit;

    private Integer sortNo = 0;

    private StatusEnum status = StatusEnum.ENABLED;
}
