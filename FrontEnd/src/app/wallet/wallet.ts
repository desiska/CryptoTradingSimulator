import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Currency } from './wallet.model';
import { HttpClient } from '@angular/common/http';
import { Authentication } from '../authentication';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-wallet',
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatSortModule
  ],
  templateUrl: './wallet.html',
  styleUrl: './wallet.css'
})
export class Wallet implements OnInit, OnDestroy {
  displayedColumns: string[] = ['currency', 'quantity'];
  dataSource = new MatTableDataSource<Currency>([]);

  isLoading: boolean = true;
  errorMessage: string | null = null;

  private userSubscription: Subscription | undefined;

  private userWalletApiUrl = 'http://localhost:8080/wallets';

  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private http: HttpClient,
    private authService: Authentication
  ) { }

  ngOnInit(): void {
    this.userSubscription = this.authService.getCurrentUser().subscribe(user => {
      if (user && user.id) {
        this.loadUserWallet(user.id);
      } else {
        this.dataSource.data = [];
        this.isLoading = false;
        this.errorMessage = 'User not logged in or ID not available. Please log in to view your wallet.';
      }
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  ngOnDestroy(): void {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  loadUserWallet(userId: number): void {
  this.isLoading = true;
  this.errorMessage = null;
  console.log('Fetching user wallet for user ID:', userId);

  this.http.get<Currency[]>(`${this.userWalletApiUrl}/${userId}`).subscribe({
    next: (currencies) => {
      console.log('Received wallet data:', currencies);
      this.dataSource.data = currencies;
      this.isLoading = false;
      if (currencies.length === 0) {
        this.errorMessage = 'No cryptocurrencies in your wallet.';
        console.log('Wallet is empty.');
      } else {
        this.errorMessage = null; 
      }
      console.log('Current dataSource.data length:', this.dataSource.data.length);
    },
    error: (error) => {
      console.error('Failed to load user wallet:', error);
      this.errorMessage = 'Failed to load wallet data. Please try again later.';
      this.isLoading = false;
    }
  });
}
}
