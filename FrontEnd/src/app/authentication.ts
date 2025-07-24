import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { UserData, UserResponse } from './user.model';

@Injectable({
  providedIn: 'root'
})
export class Authentication {
  private url = 'http://localhost:8080/auth';
  private _isLoggedIn = new BehaviorSubject<boolean>(false);
  private _user = new BehaviorSubject<UserData | null>(null);

  constructor(private http: HttpClient, private router: Router) {
    const token = this.getToken();
    const initialLoginStatus = !!token;
    this._isLoggedIn.next(initialLoginStatus);
    const storedUserData = localStorage.getItem('user_data');
    
    if(storedUserData){
      try{
        const userData: UserData = JSON.parse(storedUserData);
        this._user.next(userData);
      }
      catch (error){
        console.error('Error parsing stored user data:', error);
        localStorage.removeItem('user_data');
      }
    }
  }

  register(user: any): Observable<any> {
    console.log('Registering user:', user.username);
    return this.http.post(`${this.url}/register`, user);
  }

  login(user: any): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.url}/login`, user).pipe(
      tap(response => {
        if (response && response.token && response.id !== undefined && 
            response.username !== undefined && response.email !== undefined &&
            response.balance !== undefined) 
        {
          const userData: UserData = {
            id: response.id,
            username: response.username,
            email: response.email,
            balance: response.balance
          };

          localStorage.setItem('user_data', JSON.stringify(userData));
          localStorage.setItem('jwt_token', response.token);

          this._user.next(userData);
          this._isLoggedIn.next(true);
          console.log('Login SUCCESS! Token stored. _isLoggedIn set to TRUE.');
        } 
        else {
          console.error('Login FAILED: No token in response. _isLoggedIn set to FALSE.');
          this._isLoggedIn.next(false);
        }
        console.log('Current _isLoggedIn value AFTER login attempt:', this._isLoggedIn.getValue());
      })
    );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_data')
    this._isLoggedIn.next(false);
    this._user.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    const status = this._isLoggedIn.getValue();
    const tokenExists = !!this.getToken();
    if (status !== tokenExists) {
        console.warn('INCONSISTENCY DETECTED in isLoggedIn()! _isLoggedIn:', status, 'Token exists:', tokenExists);
        this._isLoggedIn.next(tokenExists);
        return tokenExists;
    }

    return status;
  }

  getAuthStatus(): Observable<boolean> {
    return this._isLoggedIn.asObservable();
  }

  getCurrentUser(): Observable<UserData | null> {
    return this._user.asObservable();
  }

  getStoredUser(): UserData | null {
    const userDataString = localStorage.getItem('user_data');
    return userDataString ? JSON.parse(userDataString) : null;
  }

  storeUser(user: UserData): void {
    localStorage.setItem('user_data', JSON.stringify(user));
    this._user.next(user);
  }

  updateUserBalance(newBalance: number): void {
    const currentUser = this._user.getValue();

    if(currentUser){
      const updateUser = {...currentUser, balance: newBalance};
      this.storeUser(updateUser);
    }
    else{
      console.warn('Cannot update balance: No current user data found.');
    }
  }

  updateUserDataAfterTransaction(updatedUserData: UserData): void {
      this.storeUser(updatedUserData);
  }
}