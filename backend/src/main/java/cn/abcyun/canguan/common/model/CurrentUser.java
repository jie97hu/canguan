package cn.abcyun.canguan.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {

    private Long id;
    private String username;
    private String displayName;
    private UserRole role;
    private Long storeId;
    private String storeName;
    private UserStatus status;
    private String sessionId;
}
