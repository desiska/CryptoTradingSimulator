package com.trading.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {
    private Long id;
    private TransactionType type;
    private String currency;
    private BigDecimal price;
    private BigDecimal quantity;
    private Date date;
    private Long user_id;
}
