import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { firstValueFrom, Subscription } from 'rxjs';
import { Authentication } from './authentication';
import { HttpClient } from '@angular/common/http';
import { UserData } from './user.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Table } from './table/table';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterModule,
    CommonModule,
    MatToolbarModule,
    MatButtonModule, 
    MatDividerModule, 
    MatIconModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  isLoggedIn: boolean = false;
  currentUser: UserData | null = null;

  private authStatusSubscription: Subscription | undefined;
  private userSubscription: Subscription | undefined;
  private resetUrl = 'http://localhost:8080/transactions/reset'

  constructor(
    private router: Router,
    private authService: Authentication,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.authStatusSubscription = this.authService.getAuthStatus().subscribe(status => {
      this.isLoggedIn = status;
    });

    this.userSubscription = this.authService.getCurrentUser().subscribe(user => {
      this.currentUser = user;
    });
  }

  ngOnDestroy(): void {
    if (this.authStatusSubscription) {
      this.authStatusSubscription.unsubscribe();
    }

    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  navigateToKrakenAPI(): void {
    this.router.navigate(['/table']);
  }

  navigateToTransactions(): void {
    this.router.navigate(['/transactions-history']);
  }

  navigateToWallet(): void {
    this.router.navigate(['/wallet']);
  }

  async reset(): Promise<void> {
    if (!this.isLoggedIn || !this.currentUser || !this.currentUser.id) {
      this.snackBar.open('User not logged in or ID not available to reset data.', 'Close', { duration: 3000 });
      return;
    }

    if (!confirm('Are you sure you want to reset your account data? This will delete all your cryptocurrencies and set your balance to 10000 USD.')) {
      return;
    }

    try {
      const response: any = await firstValueFrom(
        this.http.post(`${this.resetUrl}`, this.currentUser)
      );

      this.snackBar.open('Account data reset successfully!', 'Close', { duration: 5000, panelClass: ['success-snackbar'] });

      if (response && response.newBalance !== undefined) {
        this.authService.updateUserBalance(response.newBalance);
      } else if (response && response.balance !== undefined) {
        this.authService.updateUserBalance(response.balance);
      }

    } catch (error: any) {
      console.error('Failed to reset user data:', error);
      let errorMessage = 'Failed to reset account data. Please try again.';
      if (error.error && error.error.message) {
        errorMessage += ` Reason: ${error.error.message}`;
      }
      this.snackBar.open(errorMessage, 'Close', { duration: 7000, panelClass: ['error-snackbar'] });
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
