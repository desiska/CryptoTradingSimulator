package com.trading.crypto.exception;

public class NoEnoughCurrencyInWallet extends RuntimeException {
    public NoEnoughCurrencyInWallet(String message) {
        super(message);
    }
}
