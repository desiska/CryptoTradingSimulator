package com.trading.crypto.dto.AppUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppUserDto {
    private Long id;
    private String username;
    private String email;
    private BigDecimal balance;
    private String token;
}
