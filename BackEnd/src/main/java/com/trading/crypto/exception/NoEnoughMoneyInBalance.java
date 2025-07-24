package com.trading.crypto.exception;

public class NoEnoughMoneyInBalance extends RuntimeException {
    public NoEnoughMoneyInBalance(String message) {
        super(message);
    }
}
