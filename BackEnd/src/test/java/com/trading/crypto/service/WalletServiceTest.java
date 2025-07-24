package com.trading.crypto.service;

import com.trading.crypto.dto.Currency.CurrencyDto;
import com.trading.crypto.model.Currency;
import com.trading.crypto.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void testGetCurrencyWhenCurrencyExists() {
        Long userID = 1L;
        String symbol = "BTC/USD";
        Currency currency = new Currency(1L, symbol, new BigDecimal(2.5), userID);

        when(currencyRepository.findByUserIDAndSymbol(userID, symbol)).thenReturn(Optional.of(currency));

        BigDecimal result = walletService.getCurrency(userID, symbol);

        assertEquals(new BigDecimal(2.5), result);
    }

    @Test
    void testGetCurrencyWhenCurrencyDoesNotExist(){
        Long usedID = 1L;
        String symbol = "ETH/USD";

        when(currencyRepository.findByUserIDAndSymbol(usedID, symbol)).thenReturn(Optional.empty());

        BigDecimal result = walletService.getCurrency(usedID, symbol);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testGetCurrencies() {
        Long userID = 1L;
        List<Currency> currencies = Arrays.asList(
                new Currency(1L, "BTC/USD", new BigDecimal(2), userID),
                new Currency(2L, "ETH/USD", new BigDecimal(3.5), userID)
        );

        when(currencyRepository.findByUserID(userID)).thenReturn(currencies);

        List<CurrencyDto> result = walletService.getCurrencies(userID).stream().toList();

        assertEquals(2, result.size());
        assertEquals("BTC/USD", result.get(0).getCurrency());
        assertEquals("ETH/USD", result.get(1).getCurrency());
    }
}