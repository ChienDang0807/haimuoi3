import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../constants/user-role';

export const shopOwnerGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Chưa đăng nhập → redirect tới trang login kèm returnUrl
  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  }

  // Đã login nhưng không phải SHOP_OWNER → access denied
  if (authService.currentUser()?.role !== UserRole.SHOP_OWNER) {
    // Redirect to home or access denied page
    return router.createUrlTree(['/']);
  }

  return true;
};
