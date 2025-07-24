import { CanActivateFn, Router } from '@angular/router';
import { Authentication } from './authentication';
import { inject } from '@angular/core';

export const AuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(Authentication);
  const router = inject(Router);

  if(authService.isLoggedIn()){
    return true;
  }

  router.navigate(['/login']);
  return false;
};
