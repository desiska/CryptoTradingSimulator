package com.trading.crypto.service;

import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.Transaction.TransactionCreateDto;
import com.trading.crypto.dto.Transaction.TransactionDto;
import com.trading.crypto.exception.NoEnoughCurrencyInWallet;
import com.trading.crypto.exception.NoEnoughMoneyInBalance;
import com.trading.crypto.model.AppUser;
import com.trading.crypto.model.Currency;
import com.trading.crypto.model.Transaction;
import com.trading.crypto.model.TransactionType;
import com.trading.crypto.repository.AppUserRepository;
import com.trading.crypto.repository.CurrencyRepository;
import com.trading.crypto.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Long userID;
    private AppUser user;

    @BeforeEach
    void setUp(){
        userID = 1L;
        user = new AppUser();
    }

    @Test
    void testCreateBuyTransactionSuccess() {
        BigDecimal initialBalance = new BigDecimal(10000);
        user.setBalance(initialBalance);

        TransactionCreateDto dto = new TransactionCreateDto();
        dto.setUserID(userID);
        dto.setCurrency("BTC/USD");
        dto.setPrice(new BigDecimal(100));
        dto.setQuantity(new BigDecimal(2));
        dto.setType(TransactionType.BUYING);

        when(appUserRepository.findById(userID)).thenReturn(Optional.of(user));
        when(currencyRepository.findByUserIDAndSymbol(userID, "BTC/USD")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(1L);

        BigDecimal newBalance = transactionService.createBuyTransaction(dto);

        assertEquals(new BigDecimal(9800), newBalance);
        assertEquals(new BigDecimal(9800), user.getBalance());

        verify(appUserRepository).update(user);
        verify(currencyRepository).save(any());
        verify(transactionRepository).save(any());
    }

    @Test
    void testCreateBuyTransactionThrowsNoEnoughMoney() {
        user.setBalance(new BigDecimal(50));

        TransactionCreateDto dto = new TransactionCreateDto();
        dto.setUserID(userID);
        dto.setCurrency("BTC/USD");
        dto.setPrice(new BigDecimal(100));
        dto.setQuantity(new BigDecimal(2));
        dto.setType(TransactionType.BUYING);

        when(appUserRepository.findById(userID)).thenReturn(Optional.of(user));

        assertThrows(NoEnoughMoneyInBalance.class, () ->
                transactionService.createBuyTransaction(dto)
        );
    }

    @Test
    void testCreateSellTransactionSuccess() {
        user.setBalance(new BigDecimal(1000));

        when(appUserRepository.findById(userID)).thenReturn(Optional.of(user));
        when(currencyRepository.findByUserIDAndSymbol(userID, "ETH/USD"))
                .thenReturn(Optional.of(new Currency(1L, "ETH/USD", new BigDecimal(5), userID)));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(2L);

        TransactionCreateDto dto = new TransactionCreateDto();
        dto.setUserID(userID);
        dto.setCurrency("ETH/USD");
        dto.setPrice(new BigDecimal(100));
        dto.setQuantity(new BigDecimal(2));
        dto.setType(TransactionType.SELLING);

        BigDecimal updatedBalance = transactionService.createSellTransaction(dto);

        assertEquals(new BigDecimal(1200), updatedBalance);
        assertEquals(new BigDecimal(3),
                currencyRepository.findByUserIDAndSymbol(userID, "ETH/USD").get().getQuantity());

        verify(appUserRepository).update(user);
        verify(transactionRepository).save(any());
    }

    @Test
    void testCreateSellTransactionThrowsNoEnoughCurrency() {
        user.setBalance(new BigDecimal(1000));

        when(appUserRepository.findById(userID)).thenReturn(Optional.of(user));
        when(currencyRepository.findByUserIDAndSymbol(userID, "ETH/USD")).thenReturn(Optional.empty());

        TransactionCreateDto dto = new TransactionCreateDto();
        dto.setUserID(userID);
        dto.setCurrency("ETH/USD");
        dto.setPrice(new BigDecimal(100));
        dto.setQuantity(new BigDecimal(2));
        dto.setType(TransactionType.SELLING);

        // Act & Assert
        assertThrows(NoEnoughCurrencyInWallet.class, () ->
                transactionService.createSellTransaction(dto)
        );
    }

    @Test
    void testResetSuccess() {
        AppUserDto userDto = new AppUserDto();
        userDto.setId(userID);
        userDto.setBalance(new BigDecimal(5000));

        user.setBalance(new BigDecimal(5000));

        when(appUserRepository.findById(userID)).thenReturn(Optional.of(user));

        AppUserDto result = transactionService.reset(userDto);

        assertEquals(new BigDecimal(10000), result.getBalance());
        assertEquals(new BigDecimal(10000), user.getBalance());

        verify(currencyRepository).deleteByUserID(userID);
        verify(transactionRepository).deleteByUserID(userID);
        verify(appUserRepository).update(user);
    }

    @Test
    void testGetTransactions() {
        Transaction buy1 = new Transaction(1L, TransactionType.BUYING, "BTC/USD",
                new BigDecimal(10), new BigDecimal(1), new Date(1000), userID);
        Transaction buy2 = new Transaction(2L, TransactionType.BUYING, "BTC/USD",
                new BigDecimal(20), new BigDecimal(2), new Date(2000), userID);
        Transaction sell = new Transaction(3L, TransactionType.SELLING, "BTC/USD",
                new BigDecimal(30), new BigDecimal(2), new Date(3000), userID);

        when(transactionRepository.findByUserIDOrderByDateAsc(userID))
                .thenReturn(Arrays.asList(buy1, buy2, sell));

        List<TransactionDto> dtos = transactionService.getTransactions(userID);

        TransactionDto sellDto = dtos.stream()
                .filter(d -> d.getType() == TransactionType.SELLING)
                .findFirst()
                .orElseThrow();

        BigDecimal expectedCost = new BigDecimal(30);
        BigDecimal expectedRevenue = new BigDecimal(30).multiply(new BigDecimal(2)).divide(new BigDecimal(2), 0);
        expectedRevenue = new BigDecimal(60);
        BigDecimal expectedProfit = expectedRevenue.subtract(expectedCost);
        BigDecimal expectedProfitPct = expectedProfit.divide(expectedCost, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

        assertEquals(expectedProfit, sellDto.getProfitOrLoss());
        assertEquals(expectedProfitPct, sellDto.getProfitOrLossPercentage());
    }
}