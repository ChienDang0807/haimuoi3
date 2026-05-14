import { Injectable, inject } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

const AUTH_SKIP_PATHS = ['/v1/auth/login', '/v1/auth/register', '/v1/payments/webhook'];

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private authService = inject(AuthService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const isSkipped = AUTH_SKIP_PATHS.some(path => req.url.includes(path));
    if (isSkipped) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    if (!token) {
      return next.handle(req);
    }

    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
    return next.handle(cloned);
  }
}
