import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Auth } from './auth';
import { API_URL } from './api-config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const router = inject(Router);

  const isApiRequest = req.url.startsWith(API_URL);
  const authHeader = auth.getAuthHeader();

  const authReq = isApiRequest && authHeader ? req.clone({ setHeaders: { Authorization: authHeader } }) : req;

  return next(authReq).pipe(
    catchError((error) => {
      if (isApiRequest && error.status === 401) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
