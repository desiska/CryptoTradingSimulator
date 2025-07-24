import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Authentication } from '../authentication';
import { Table } from '../table/table';

@Component({
  selector: 'app-home',
  imports: [
    Table
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit{
  securedMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient, private authService: Authentication){

  }
  ngOnInit(): void {
    this.fetchSecuredData();
  }

  fetchSecuredData(): void{
    const token = this.authService.getToken();

    if(token){
      const headers = new HttpHeaders().set('Authotization', 'Bearer ${token}');
      this.http.get('http://localhost:8080/secured', { headers, responseType: 'text' })
        .subscribe({
          next: (response) => {
            this.securedMessage = response;
            this.errorMessage = '';
          },
          error: () => {
            this.securedMessage = '';
            this.errorMessage = 'Could not fetch secured data. You might not be authorized.';
          }
        });
    }
    else{
      this.errorMessage = 'You are not logged in.';
    }
  }

  logout(): void{
    this.authService.logout();
  }
}
