import { Component } from '@angular/core';
import { Authentication } from '../authentication';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  user = { username: '', email: '', password: '', }
  successMessage: string = '';
  errorMessage: string = '';
  hidePassword = true;
  
  constructor(private authService: Authentication, private router: Router){
    
  }

  onSubmit(): void{
    if (!this.user.username || !this.user.email || !this.user.password) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    this.authService.register(this.user).subscribe({
      next: (response) => {
        this.successMessage = 'Registration successfull! You can now log in.';
        this.errorMessage = '';
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.successMessage = '';
        this.errorMessage = error.error || 'Registration failed. Please try again.';
      }
    });
  }
}
