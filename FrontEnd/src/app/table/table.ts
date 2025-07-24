import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Ticker } from './ticker.model';
import { interval, startWith, Subscription, switchMap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Transaction } from '../transaction/transaction';
import { Router } from '@angular/router';
import { Authentication } from '../authentication';

@Component({
  selector: 'app-table',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatDialogModule,
    MatSortModule
  ],
  templateUrl: './table.html',
  styleUrl: './table.css'
})
export class Table implements OnInit, OnDestroy {
  displayedColumns: string[] = ['symbol', 'bid', 'ask'];
  dataSource = new MatTableDataSource<Ticker>([]);
  private updateSubscription: Subscription | undefined;
  private authStatusSubscription: Subscription | undefined;

  @ViewChild(MatSort) sort!: MatSort;

  constructor(private http: HttpClient, private dialog: MatDialog, 
      private authService: Authentication, private router: Router){

  }

  ngOnInit(): void {
    this.updateDisplayedColumns();

    this.authStatusSubscription = this.authService.getAuthStatus().subscribe(
      () => { 
        this.updateDisplayedColumns() 
        //this.dataSource.data = [...this.dataSource.data];
      });

    this.updateSubscription = interval(3000)
      .pipe(
        startWith(0),
        switchMap(() => this.http.get<Ticker[]>('http://localhost:8080/tickers'))
      )
      .subscribe(
        (data: Ticker[]) => {
          this.dataSource.data = data;
        },
        error => {
          console.error('Error fetching tickers: ', error);
        }
      );
  }

  ngAfterViewInit(){
    this.dataSource.sort = this.sort;
  }

  isLoggedIn(): boolean{
    return this.authService.isLoggedIn();
  }

  private updateDisplayedColumns(): void{
    const actionsColumnExists = this.displayedColumns.includes('actions');

    if (this.isLoggedIn()) {
      if (!actionsColumnExists) {
        this.displayedColumns.push('actions');
        (this.dataSource as MatTableDataSource<any>)._updateChangeSubscription(); // <-- ADD THIS
      }
    } else {
      if (actionsColumnExists) {
        this.displayedColumns = this.displayedColumns.filter(col => col !== 'actions');
        (this.dataSource as MatTableDataSource<any>)._updateChangeSubscription(); // <-- ADD THIS
      }
    }
  }

  applyFilter(event: Event){
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  buy(element: Ticker): void{
    if(this.isLoggedIn()){
      this.openTransactionDialog(element, 'buy');
    }
    else{
      console.warn('Attempted to buy while not logged in. Redirecting to login.');
      this.router.navigate(['/login'])
    }
  }

  sell(element: Ticker): void{
    if(this.isLoggedIn()){
      this.openTransactionDialog(element, 'sell');
    }
    else{
      console.warn('Attempted to sell while not logged in. Redirecting to login.');
      this.router.navigate(['/login'])
    }
  }

  openTransactionDialog(ticker: Ticker, type: 'buy' | 'sell'): void{
    const dialogRef = this.dialog.open(Transaction, {
      data: {ticker: ticker, type: type}
    });

    dialogRef.afterClosed().subscribe();
  }

  ngOnDestroy(): void {
    if(this.updateSubscription){
      this.updateSubscription.unsubscribe();
    }

    if(this.authStatusSubscription){
      this.authStatusSubscription.unsubscribe();
    }
  }
}
