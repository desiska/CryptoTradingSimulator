package com.trading.crypto.service;

import com.trading.crypto.dto.KrakenTickerDto.TickerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class TickerStoreTest {
    private TickerStore tickerStore;

    @BeforeEach
    void setUp() {
        tickerStore = new TickerStore();
    }

    @Test
    void testUpdateTickerNewCurrency() {
        TickerDto ticker = new TickerDto("BTC/USD", new BigDecimal(30000), new BigDecimal(30100));
        tickerStore.updateTicker(ticker);

        Collection<TickerDto> tickers = tickerStore.getAllTickers();
        assertEquals(1, tickers.size());

        TickerDto stored = tickers.iterator().next();
        assertEquals("BTC/USD", stored.getSymbol());
        assertEquals(new BigDecimal(30000), stored.getBid());
        assertEquals(new BigDecimal(30100), stored.getAsk());
    }

    @Test
    void testUpdateTickerOverwriteCurrency() {
        TickerDto first = new TickerDto("ETH/USD", new BigDecimal(1900), new BigDecimal(1910));
        TickerDto second = new TickerDto("ETH/USD", new BigDecimal(1950), new BigDecimal(1960));

        tickerStore.updateTicker(first);
        tickerStore.updateTicker(second);

        Collection<TickerDto> tickers = tickerStore.getAllTickers();
        assertEquals(1, tickers.size());

        TickerDto stored = tickers.iterator().next();
        assertEquals(new BigDecimal(1950), stored.getBid());
        assertEquals(new BigDecimal(1960), stored.getAsk());
    }

    @Test
    void testGetAllTickersEmptyInitially() {
        assertTrue(tickerStore.getAllTickers().isEmpty());
    }
}