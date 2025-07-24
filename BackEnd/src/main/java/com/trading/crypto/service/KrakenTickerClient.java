package com.trading.crypto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.crypto.dto.KrakenTickerDto.TickerDto;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Service
public class KrakenTickerClient implements WebSocket.Listener {

    private static final String WS_URL = "wss://ws.kraken.com/v2";

    private static final List<String> PAIRS = List.of("BTC/USD",
            "ETH/USD", "ADA/USD", "SOL/USD", "XRP/USD", "DOT/USD", "POL/USD",
            "LTC/USD", "TRX/USD", "SHIB/USD", "AVAX/USD", "UNI/USD", "LINK/USD",
            "DOGE/USD", "ETC/USD", "ATOM/USD", "OP/USD", "ARB/USD", "IMX/USD",
            "APE/USD");

    //private static final List<String> PAIRS = List.of("ADA/USD");

    //private static final List<String> PAIRS = List.of("XBT/USD", "ETH/USD", "ADA/USD", "SOL/USD");

    private final ObjectMapper mapper;
    private final TickerStore tickerStore;
    private WebSocket webSocket;

    public KrakenTickerClient(TickerStore tickerStore){
        this.mapper = new ObjectMapper();
        this.tickerStore = tickerStore;
    }

    @PostConstruct
    public void init() {
        connect();
    }

    private void connect() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create(WS_URL), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("Connected to Kraken WebSocket.");
                    subscribe();
                })
                .exceptionally(ex -> {
                    System.err.println("Failed to connect: " + ex.getMessage());
                    return null;
                });
    }

    private void subscribe() {
        try {
            String subscribeMsg = """
            {
              "method":"subscribe",
              "params":{
                "channel":"ticker",
                "symbol":%s
              }
            }
            """.formatted(mapper.writeValueAsString(PAIRS));

            webSocket.sendText(subscribeMsg, true);
            System.out.println("Subscribed to ticker for top pairs.");
        } catch (Exception e) {
            System.err.println("Subscribe error: " + e.getMessage());
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        handleIncoming(data.toString());
        ws.request(1);
        return null;
    }

    private void handleIncoming(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            //System.out.println("----" + node);

            if(node.get("channel").asText().equals("ticker")){
                //System.out.println("Ack: " + json);

                JsonNode d = node.get("data").get(0);
                String symbol = d.get("symbol").asText();

                BigDecimal bid = BigDecimal.valueOf(d.hasNonNull("bid") ? d.get("bid").asDouble() : Double.NaN);
                BigDecimal ask = BigDecimal.valueOf(d.hasNonNull("ask") ? d.get("ask").asDouble() : Double.NaN);

                tickerStore.updateTicker(new TickerDto(symbol, bid, ask));

                //System.out.printf("[%s] Bid: %f, Ask: %f%n", symbol, bid, ask);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket opened.");
        webSocket.request(1);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.err.println("WebSocket closed: " + statusCode + " " + reason);
        try{
            Thread.sleep(5000);
            connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Reconnect attempt interrupted.");
        }
        return null;
    }
}
