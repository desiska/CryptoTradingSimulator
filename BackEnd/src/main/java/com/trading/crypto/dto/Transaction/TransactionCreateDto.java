package com.trading.crypto.dto.Transaction;

import com.trading.crypto.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionCreateDto {
    private TransactionType type;
    private String currency;
    private BigDecimal price;
    private BigDecimal quantity;
    private Long userID;
}
