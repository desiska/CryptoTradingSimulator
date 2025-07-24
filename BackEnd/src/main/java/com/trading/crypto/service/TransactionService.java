package com.trading.crypto.service;

import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.Currency.CurrencyDto;
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
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.OperationsException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.text.DateFormat.getDateTimeInstance;

@Service
public class TransactionService {
    private TransactionRepository transactionRepository;
    private CurrencyRepository currencyRepository;
    private AppUserRepository appUserRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              CurrencyRepository currencyRepository, AppUserRepository appUserRepository){
        this.transactionRepository = transactionRepository;
        this.currencyRepository = currencyRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public BigDecimal createBuyTransaction(TransactionCreateDto transactionCreateDto){
        Currency currency = this.currencyRepository
                .findByUserIDAndSymbol(transactionCreateDto.getUserID(), transactionCreateDto.getCurrency())
                .orElse(null);

        AppUser user = this.appUserRepository
                .findById(transactionCreateDto.getUserID())
                .orElseThrow();

        BigDecimal totalPrice = transactionCreateDto.getPrice().multiply(transactionCreateDto.getQuantity());

        if(user.getBalance().compareTo(totalPrice) < 0){
            throw new NoEnoughMoneyInBalance("You cannot buy crypto because you haven't enough money!");
        }

        user.setBalance(user.getBalance().subtract(totalPrice));
        this.appUserRepository.update(user);

        if(currency == null){
            currency = new Currency(-1L, transactionCreateDto.getCurrency(),
                    transactionCreateDto.getQuantity(), transactionCreateDto.getUserID());

            this.currencyRepository.save(currency);
        }
        else{
            currency.setQuantity(currency.getQuantity().add(transactionCreateDto.getQuantity()));
            this.currencyRepository.update(currency);
        }

        Transaction transaction = new Transaction(-1L, transactionCreateDto.getType(),
                transactionCreateDto.getCurrency(), transactionCreateDto.getPrice(),
                transactionCreateDto.getQuantity(), null, transactionCreateDto.getUserID());

        transaction.setId(this.transactionRepository.save(transaction));

        return user.getBalance();
    }

    @Transactional
    public BigDecimal createSellTransaction(TransactionCreateDto transactionCreateDto){
        Currency currency = this.currencyRepository
                .findByUserIDAndSymbol(transactionCreateDto.getUserID(), transactionCreateDto.getCurrency())
                .orElse(null);

        AppUser user = this.appUserRepository
                .findById(transactionCreateDto.getUserID())
                .orElseThrow();

        BigDecimal totalPrice = transactionCreateDto.getPrice().multiply(transactionCreateDto.getQuantity());

        if(currency == null || currency.getQuantity().compareTo(transactionCreateDto.getQuantity()) < 0){
            throw new NoEnoughCurrencyInWallet("You don't have enough quantity to sell from this currency.");
        }

        user.setBalance(user.getBalance().add(totalPrice));
        this.appUserRepository.update(user);

        currency.setQuantity(currency.getQuantity().subtract(transactionCreateDto.getQuantity()));

        if (BigDecimal.ZERO.compareTo(currency.getQuantity()) == 0) {
            this.currencyRepository.deleteById(currency.getId());
        }
        else {
            this.currencyRepository.update(currency);
        }

        Transaction transaction = new Transaction(-1L, transactionCreateDto.getType(),
                transactionCreateDto.getCurrency(), transactionCreateDto.getPrice(),
                transactionCreateDto.getQuantity(), null, transactionCreateDto.getUserID());

        transaction.setId(this.transactionRepository.save(transaction));

        return user.getBalance();
    }

    @Transactional
    public AppUserDto reset(AppUserDto userDto){
        this.currencyRepository.deleteByUserID(userDto.getId());
        this.transactionRepository.deleteByUserID(userDto.getId());

        userDto.setBalance(new BigDecimal(10000));

        AppUser user = this.appUserRepository.findById(userDto.getId()).orElse(null);
        user.setBalance(new BigDecimal(10000));
        this.appUserRepository.update(user);

        return userDto;
    }

    private static class BoughtLot {
        BigDecimal quantityRemaining;
        BigDecimal purchasePrice;

        public BoughtLot(BigDecimal quantity, BigDecimal price) {
            this.quantityRemaining = quantity;
            this.purchasePrice = price;
        }
    }

    public List<TransactionDto> getTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIDOrderByDateAsc(userId);
        Map<String, Queue<BoughtLot>> currencyLots = new HashMap<>();
        List<TransactionDto> transactionDtos = new ArrayList<>();

        for (Transaction transaction : transactions) {
            TransactionDto dto;

            if (transaction.getType() == TransactionType.BUYING) {
                currencyLots.computeIfAbsent(transaction.getCurrency(), k -> new LinkedList<>())
                        .add(new BoughtLot(transaction.getQuantity(), transaction.getPrice()));

                dto = new TransactionDto(
                        transaction.getId(),
                        transaction.getType(),
                        transaction.getCurrency(),
                        transaction.getPrice(),
                        transaction.getQuantity(),
                        transaction.getDate(),
                        transaction.getUser_id(),
                        null,
                        null
                );
            }
            else if (transaction.getType() == TransactionType.SELLING) {
                BigDecimal soldQuantity = transaction.getQuantity();
                BigDecimal salePrice = transaction.getPrice();
                BigDecimal totalCostBasis = BigDecimal.ZERO;
                BigDecimal remainingSoldQuantity = soldQuantity;

                Queue<BoughtLot> lots = currencyLots.get(transaction.getCurrency());

                if (lots == null || lots.isEmpty()) {
                    totalCostBasis = BigDecimal.ZERO;
                }
                else {
                    while (remainingSoldQuantity.compareTo(BigDecimal.ZERO) > 0 && !lots.isEmpty()) {
                        BoughtLot currentLot = lots.peek();

                        if (currentLot.quantityRemaining.compareTo(remainingSoldQuantity) >= 0) {
                            totalCostBasis = totalCostBasis.add(remainingSoldQuantity.multiply(currentLot.purchasePrice));
                            currentLot.quantityRemaining = currentLot.quantityRemaining.subtract(remainingSoldQuantity);
                            remainingSoldQuantity = BigDecimal.ZERO;

                            if (currentLot.quantityRemaining.compareTo(BigDecimal.ZERO) == 0) {
                                lots.poll();
                            }
                        } else {
                            totalCostBasis = totalCostBasis.add(currentLot.quantityRemaining.multiply(currentLot.purchasePrice));
                            remainingSoldQuantity = remainingSoldQuantity.subtract(currentLot.quantityRemaining);
                            lots.poll();
                        }
                    }
                }

                BigDecimal revenue = salePrice.multiply(soldQuantity);
                BigDecimal profitLoss = revenue.subtract(totalCostBasis);
                BigDecimal profitLossPercentage = BigDecimal.ZERO;

                if (totalCostBasis.compareTo(BigDecimal.ZERO) != 0) {
                    profitLossPercentage = profitLoss.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                }

                dto = new TransactionDto(
                        transaction.getId(),
                        transaction.getType(),
                        transaction.getCurrency(),
                        transaction.getPrice(),
                        transaction.getQuantity(),
                        transaction.getDate(),
                        transaction.getUser_id(),
                        profitLoss,
                        profitLossPercentage
                );
            }
            else {
                dto = new TransactionDto(
                        transaction.getId(),
                        transaction.getType(),
                        transaction.getCurrency(),
                        transaction.getPrice(),
                        transaction.getQuantity(),
                        transaction.getDate(),
                        transaction.getUser_id(),
                        null,
                        null
                );
            }

            transactionDtos.add(dto);
        }

        transactionDtos.sort(Comparator.comparing(TransactionDto::getDate).reversed());

        return transactionDtos;
    }
}
