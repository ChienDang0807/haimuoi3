import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../constants/user-role';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Chưa đăng nhập → redirect tới trang login kèm returnUrl
  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  }

  // Đã login nhưng không phải ADMIN → access denied
  if (authService.currentUser()?.role !== UserRole.ADMIN) {
    return router.createUrlTree(['/sysadmin/access-denied']);
  }

  return true;
};
