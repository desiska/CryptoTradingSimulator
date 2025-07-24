package com.trading.crypto.dto.Transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.crypto.model.Transaction;
import com.trading.crypto.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionDto {
    private Long id;
    private TransactionType type;
    private String currency;
    private BigDecimal price;
    private BigDecimal quantity;
    private Date date;
    private Long userID;
    private BigDecimal profitOrLoss;
    private BigDecimal profitOrLossPercentage;

    public TransactionDto(Transaction transaction) {
        this.id = transaction.getId();
        this.type = transaction.getType();
        this.currency = transaction.getCurrency();
        this.price = transaction.getPrice();
        this.quantity = transaction.getQuantity();
        this.date = transaction.getDate();
        this.userID = transaction.getUser_id();
    }
}
