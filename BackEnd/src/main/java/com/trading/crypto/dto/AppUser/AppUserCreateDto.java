package com.trading.crypto.dto.AppUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppUserCreateDto {
    private String username;
    private String password;
    private String email;
}
