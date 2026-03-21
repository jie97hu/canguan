package cn.abcyun.canguan.expense.store.dto;

import java.time.LocalDateTime;

import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;

@Data
public class StoreDto {

    private Long id;

    private String name;

    private String code;

    private StatusEnum status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
