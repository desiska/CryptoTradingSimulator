package com.trading.crypto.controller;

import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.Transaction.TransactionCreateDto;
import com.trading.crypto.dto.Transaction.TransactionDto;
import com.trading.crypto.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;

@RestController
@RequestMapping("transactions")
@CrossOrigin(origins = "http://localhost:4200")
public class TransactionController {
    private TransactionService service;

    @Autowired
    public TransactionController(TransactionService service){
        this.service = service;
    }

    @GetMapping("/{userID}")
    public ResponseEntity<Collection<TransactionDto>> getAllTransaction(@PathVariable Long userID){
        Collection<TransactionDto> transactionDtos = this.service.getTransactions(userID);

        return new ResponseEntity<>(transactionDtos, HttpStatus.OK);
    }

    @PostMapping("/buy")
    public ResponseEntity<BigDecimal> createBuyTransaction(@RequestBody TransactionCreateDto transactionCreateDto){
        return new ResponseEntity<>(this.service.createBuyTransaction(transactionCreateDto), HttpStatus.CREATED);
    }

    @PostMapping("/sell")
    public ResponseEntity<BigDecimal> createSellTransaction(@RequestBody TransactionCreateDto transactionCreateDto){
        return new ResponseEntity<>(this.service.createSellTransaction(transactionCreateDto), HttpStatus.CREATED);
    }

    @PostMapping("/reset")
    public ResponseEntity<AppUserDto> reset(@RequestBody AppUserDto appUserDto){
        AppUserDto appUserDtoResult = this.service.reset(appUserDto);

        return new ResponseEntity<>(appUserDtoResult, HttpStatus.OK);
    }
}
