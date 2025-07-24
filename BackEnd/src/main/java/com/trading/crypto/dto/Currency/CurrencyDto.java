package com.trading.crypto.dto.Currency;

import com.trading.crypto.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CurrencyDto {
    private Long id;
    private String currency;
    private BigDecimal quantity;
    private Long userID;

    public CurrencyDto(Currency currency){
        this.setId(currency.getId());
        this.setCurrency(currency.getCurrency());
        this.setQuantity(currency.getQuantity());
        this.setUserID(currency.getUser_id());
    }
}
