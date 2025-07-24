import { Ticker } from "../table/ticker.model";

export interface TransactionData{
    ticker: Ticker;
    type: 'buy' | 'sell';
}