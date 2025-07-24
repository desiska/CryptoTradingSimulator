package com.trading.crypto.service;

import com.trading.crypto.dto.Currency.CurrencyDto;
import com.trading.crypto.model.Currency;
import com.trading.crypto.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;

@Service
public class WalletService {
    private CurrencyRepository currencyRepository;
    @Autowired
    public WalletService(CurrencyRepository currencyRepository){
        this.currencyRepository = currencyRepository;
    }

    public BigDecimal getCurrency(Long userID, String currencySymbol){
        Currency currency = this.currencyRepository
                .findByUserIDAndSymbol(userID, currencySymbol)
                .orElse(null);

        return currency == null ? new BigDecimal(0) : currency.getQuantity();
    }

    public Collection<CurrencyDto> getCurrencies(Long userID){
        return this.currencyRepository
                .findByUserID(userID)
                .stream()
                .map(CurrencyDto::new)
                .toList();
    }
}
