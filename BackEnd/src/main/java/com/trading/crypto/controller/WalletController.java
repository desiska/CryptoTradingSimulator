package com.trading.crypto.controller;

import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.Currency.CurrencyDto;
import com.trading.crypto.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;

@RestController
@RequestMapping("wallets")
@CrossOrigin(origins = "http://localhost:4200")
public class WalletController {
    private WalletService service;

    @Autowired
    public WalletController(WalletService service){
        this.service = service;
    }

    @GetMapping("/{userID}/{baseCurrency}/{quoteCurrency}")
    public BigDecimal getCurrency(@PathVariable Long userID, @PathVariable String baseCurrency, @PathVariable String quoteCurrency){
        String currency = baseCurrency + '/' + quoteCurrency;
        return this.service.getCurrency(userID, currency);
    }

    @GetMapping("/{userID}")
    public ResponseEntity<Collection<CurrencyDto>> getWallet(@PathVariable Long userID){
        Collection<CurrencyDto> currencyDtos = this.service.getCurrencies(userID);

        return new ResponseEntity<>(currencyDtos, HttpStatus.OK);
    }
}
