import { Component, signal } from '@angular/core';
import { Authentication } from '../authentication';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule, 
    MatDividerModule, 
    MatIconModule,
    MatFormFieldModule, 
    MatInputModule, 
    MatSelectModule,
    MatCardModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  user = {username: '', password: ''};
  errorMessage: string = '';

  constructor(private authService: Authentication, private router: Router){

  }

  onSubmit(): void{
    this.authService.login(this.user).subscribe({
      next: (response) => {
        console.log('Login');
        this.router.navigate(['/table']);
      },
      error: (error) => {
        this.errorMessage = error.error || 'Login failed. Please try again.';
      }
    })
  }

  hide = signal(true);
  clickEvent(event: MouseEvent) {
    this.hide.set(!this.hide());
    event.stopPropagation();
  }
}
