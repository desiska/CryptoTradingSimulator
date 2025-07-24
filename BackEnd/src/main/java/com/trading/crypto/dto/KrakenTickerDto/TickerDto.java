package com.trading.crypto.dto.KrakenTickerDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TickerDto {
    private String symbol;
    private BigDecimal bid;
    private BigDecimal ask;
}
