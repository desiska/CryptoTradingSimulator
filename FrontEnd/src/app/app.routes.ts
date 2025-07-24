import { Routes } from '@angular/router';
import { Table } from './table/table';
import { Transaction } from './transaction/transaction';
import { Login } from './login/login';
import { Register } from './register/register';
import { Home } from './home/home';
import { TransactionsHistory } from './transactions-history/transactions-history';
import { Wallet } from './wallet/wallet';

export const routes: Routes = [
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'table', component: Table },
    { path: 'transaction', component: Transaction },
    { path: 'login', component: Login },
    { path: 'register', component: Register },
    { path: 'home', component: Home },
    { path: 'transactions-history', component: TransactionsHistory },
    { path: 'wallet', component: Wallet },
    { path: '**', redirectTo: '/home' }
];
