import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TransactionData } from './transaction.model';
import { Authentication } from '../authentication';
import { UserData } from '../user.model';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'app-transaction',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatDialogModule
  ],
  templateUrl: './transaction.html',
  styleUrl: './transaction.css'
})
export class Transaction implements OnInit, OnDestroy {
  currency: string = '';
  quantity: number | undefined;
  price: number;
  type: 'buy' | 'sell';
  currentUser: UserData | null = null;
  private userSubscription: Subscription | undefined;


  constructor(
    public dialogRef: MatDialogRef<Transaction>,
    @Inject(MAT_DIALOG_DATA)
    public data: TransactionData,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private authService: Authentication
  ){
    this.currency = data.ticker.symbol;
    this.type = data.type;
    this.price = this.type === 'buy' ? data.ticker.ask : data.ticker.bid;
  }

  ngOnInit(): void {
    this.userSubscription = this.authService.getCurrentUser()
      .subscribe(user => { this.currentUser = user; });
  }

  ngOnDestroy(): void {
    if(this.userSubscription){
      this.userSubscription.unsubscribe();
    }
  }

  async performTransaction(): Promise<void> {
    if(!this.quantity || this.quantity <= 0){
      this.snackBar.open("Please enter valid number.", 'Close', {duration: 3000});
      return;
    }

    if (!this.currentUser || this.currentUser.balance === undefined || this.currentUser.id === undefined) {
      this.snackBar.open('User data not loaded. Please try again or log in.', 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
      return;
    }

    if (this.type === 'buy') {
      if (this.quantity * this.price > this.currentUser.balance) {
        this.snackBar.open(`Insufficient balance. You need ${ this.quantity * this.price } but have ${this.currentUser.balance}.`, 'Close', { duration: 7000, panelClass: ['error-snackbar'] });
        return;
      }
    }
    else if(this.type === 'sell'){
      try{
        const currentCryptoHolding = await firstValueFrom(
          this.http.get<number>(`http://localhost:8080/wallets/${this.currentUser.id}/${this.currency}`)
        );

        console.log(currentCryptoHolding);

        if (currentCryptoHolding === null || currentCryptoHolding === undefined || isNaN(currentCryptoHolding)) {
          this.snackBar.open(`Failed to load your ${this.currency} in wallet.`, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
          return;
        }

        if (this.quantity > currentCryptoHolding) {
          this.snackBar.open(`Insufficient ${this.currency} in wallet. You have ${currentCryptoHolding} but trying to sell ${this.quantity}.`, 'Close', { duration: 7000, panelClass: ['error-snackbar'] });
          return;
        }
      }
      catch (error){
        console.error('Error fetching crypto holding for sell validation:', error);
        this.snackBar.open(`Failed to verify your ${this.currency} in wallet. Please try again.`, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
        return;
      }
    }
    
    const transactionUrl = `http://localhost:8080/transactions/${this.type}`;
    const transactionPayload = {
      currency: this.currency,
      quantity: this.quantity,
      price: this.price,
      type: this.type === 'buy' ? 'BUYING' : 'SELLING',
      userID: this.currentUser.id
    };

    this.http.post<any>(transactionUrl, transactionPayload).subscribe({
      next: (newBalance) => {
        this.snackBar.open(`${this.type === 'buy' ? 'Buy' : 'Sell'} transaction successful!`, 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
        
        this.authService.updateUserBalance(newBalance);
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.log('Transaction failed:', error);
        this.snackBar.open(error.error, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
        this.dialogRef.close(false);
      }
    })

    console.log('Sending transaction:', transactionPayload);
  }

  onCancel(): void{
    this.dialogRef.close();
  }
}
