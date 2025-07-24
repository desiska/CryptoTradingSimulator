package com.trading.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppUser {
    private Long id;
    private String username;
    private String email;
    private String password;
    private BigDecimal balance;
}
