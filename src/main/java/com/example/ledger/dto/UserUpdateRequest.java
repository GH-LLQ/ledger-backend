// UserUpdateRequest.java
package com.example.ledger.dto;
import lombok.Data;
@Data
public class UserUpdateRequest {
    private String email;
    // 后续如果有头像字段也可以放这里
}