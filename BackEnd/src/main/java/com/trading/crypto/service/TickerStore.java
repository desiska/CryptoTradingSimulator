package com.trading.crypto.service;

import com.trading.crypto.dto.KrakenTickerDto.TickerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TickerStore {
    private final Map<String, TickerDto> latestTickers;

    @Autowired
    public TickerStore(){
        this.latestTickers = new ConcurrentHashMap<>();
    }

    public void updateTicker(TickerDto ticker){
        latestTickers.put(ticker.getSymbol(), ticker);
    }

    public Collection<TickerDto> getAllTickers(){
        return latestTickers.values();
    }
}
