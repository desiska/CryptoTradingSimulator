import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Authentication } from '../authentication';
import { Subscription } from 'rxjs';

import { Transaction } from './transactions-history.model';

@Component({
  selector: 'app-transactions-history',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatSortModule
  ],
  templateUrl: './transactions-history.html',
  styleUrl: './transactions-history.css'
})

export class TransactionsHistory implements OnInit, OnDestroy {
  displayedColumns: string[] = ['currency', 'type', 'price', 'quantity', 'date', 'profitOrLoss', 'profitOrLossPercentage'];
  dataSource = new MatTableDataSource<Transaction>([]);
  isLoading: boolean = true;
  errorMessage: string | null = null;

  private userSubscription: Subscription | undefined;
  private transactionsHistoryUrl = 'http://localhost:8080/transactions'; 

  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private http: HttpClient,
    private authService: Authentication
  ) { }

  ngOnInit(): void {
    this.userSubscription = this.authService.getCurrentUser().subscribe(user => {
      if (user && user.id) {
        this.loadTransactionsHistory(user.id);
      } else {
        this.dataSource.data = [];
        this.isLoading = false;
        this.errorMessage = 'User not logged in or ID not available. Please log in to view transaction history.';
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

  loadTransactionsHistory(userId: number): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.http.get<Transaction[]>(`${this.transactionsHistoryUrl}/${userId}`).subscribe({
      next: (transactions) => {
        this.dataSource.data = transactions;
        console.log(transactions[0]);
        this.isLoading = false;
        if (transactions.length === 0) {
          this.errorMessage = 'No transactions found for this user.';
        }
      },
      error: (error) => {
        console.error('Failed to load transaction history:', error);
        this.errorMessage = 'Failed to load transaction history. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  getProfitLossMethod(value: number | undefined): string {
    if (value === undefined || value === null) {
      return '';
    }
    return value > 0 ? 'profit' : value < 0 ? 'loss' : 'neutral';
  }
}