export interface Transaction{
    currency: string,
    type: 'buying' | 'selling';
    price: number,
    quantity: number,
    date: Date,
    profitOrLoss?: number,
    profitOrLossPercentage?: number,
    currentMarketPrice?: number
}