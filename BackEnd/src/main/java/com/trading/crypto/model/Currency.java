package com.trading.crypto.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Currency {
    private Long id;
    private String currency;
    private BigDecimal quantity;
    private Long user_id;
}
