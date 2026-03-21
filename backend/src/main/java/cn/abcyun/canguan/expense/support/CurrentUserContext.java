package cn.abcyun.canguan.expense.support;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CurrentUserContext implements Serializable {

    private Long id;

    private String username;

    private String displayName;

    private UserRoleEnum role;

    private Long storeId;

    private String storeName;

    private StatusEnum status;

    private Long tokenVersion;

    private LocalDateTime updatedAt;
}
