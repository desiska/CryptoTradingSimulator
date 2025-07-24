package com.trading.crypto.controller;

import com.trading.crypto.dto.KrakenTickerDto.TickerDto;
import com.trading.crypto.service.TickerStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("tickers")
@CrossOrigin(origins = "http://localhost:4200")
public class TickerController {
    private final TickerStore tickerStore;

    @Autowired
    public TickerController(TickerStore tickerStore){
        this.tickerStore = tickerStore;
    }

    @GetMapping
    public Collection<TickerDto> getAllTickers(){
        return tickerStore.getAllTickers();
    }
}
