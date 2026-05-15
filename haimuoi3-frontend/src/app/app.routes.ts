import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ProductDetailPageComponent } from './features/product-detail/product-detail-page/product-detail-page.component';
import { ShopDetailPageComponent } from './features/shop-detail/shop-detail-page/shop-detail-page.component';
import { DashboardPageComponent } from './features/admin/dashboard/dashboard-page.component';
import { InventoryPageComponent } from './features/admin/inventory/inventory-page.component';
import { OrdersPageComponent } from './features/admin/orders/orders-page.component';
import { ProductsPageComponent } from './features/admin/products/products-page.component';
import { SettingsPageComponent } from './features/admin/settings/settings-page.component';
import { CategoriesPageComponent } from './features/admin/categories/categories-page.component';
import { AdminOrderDetailPageComponent } from './features/admin/order-detail/admin-order-detail-page.component';
import { Dashboard as SysAdminDashboardComponent } from './features/sysadmin/dashboard/dashboard';
import { Users as SysAdminUsersComponent } from './features/sysadmin/users/users';
import { AccessDenied as SysAdminAccessDeniedComponent } from './features/sysadmin/access-denied/access-denied';
import { CartPageComponent } from './features/cart/cart-page/cart-page.component';
import { OrderCheckoutPageComponent } from './features/checkout/order-checkout-page/order-checkout-page.component';
import { OrderConfirmationPageComponent } from './features/checkout/order-confirmation-page/order-confirmation-page.component';
import { CategoryCollectionPageComponent } from './features/category/category-collection-page/category-collection-page.component';
import { LoginPageComponent } from './features/auth/login-page/login-page.component';
import { RegisterPageComponent } from './features/auth/register-page/register-page.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { shopOwnerGuard } from './core/guards/shop-owner.guard';
import { AccountProfilePageComponent } from './features/account/account-profile-page/account-profile-page.component';
import { AccountSettingsPageComponent } from './features/account/account-settings-page/account-settings-page.component';
import { AccountOrdersPageComponent } from './features/account/account-orders-page/account-orders-page.component';
import { AccountOrderDetailPageComponent } from './features/account/account-order-detail-page/account-order-detail-page.component';
import { AccountPaymentMethodsPageComponent } from './features/account/account-payment-methods-page/account-payment-methods-page.component';
import { AccountWishlistPageComponent } from './features/account/account-wishlist-page/account-wishlist-page.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    title: 'haimuoi2 | Precision Redefined'
  },
  {
    path: 'login',
    component: LoginPageComponent,
    title: 'Sign In | haimuoi2'
  },
  {
    path: 'register',
    component: RegisterPageComponent,
    title: 'Create Account | haimuoi2'
  },
  {
    path: 'product/:id',
    component: ProductDetailPageComponent,
    title: 'Product Details | haimuoi2'
  },
  {
    path: 'shop/:id',
    component: ShopDetailPageComponent,
    title: 'Shop Details | haimuoi2'
  },
  {
    path: 'cart',
    component: CartPageComponent,
    title: 'Cart | haimuoi2'
  },
  {
    path: 'category/:slug',
    component: CategoryCollectionPageComponent,
    title: 'Collections | haimuoi2'
  },
  {
    path: 'checkout',
    component: OrderCheckoutPageComponent,
    canActivate: [authGuard],
    title: 'Checkout | haimuoi2'
  },
  {
    path: 'order-confirmation',
    component: OrderConfirmationPageComponent,
    title: 'Order Confirmation | haimuoi2'
  },
  {
    path: 'account',
    redirectTo: 'account/profile',
    pathMatch: 'full',
  },
  {
    path: 'account/profile',
    component: AccountProfilePageComponent,
    canActivate: [authGuard],
    title: 'Personal Profile | haimuoi2',
  },
  {
    path: 'account/settings',
    component: AccountSettingsPageComponent,
    canActivate: [authGuard],
    title: 'Account Settings | haimuoi2',
  },
  {
    path: 'account/orders/:id',
    component: AccountOrderDetailPageComponent,
    canActivate: [authGuard],
    title: 'Order Details | haimuoi2',
  },
  {
    path: 'account/orders',
    component: AccountOrdersPageComponent,
    canActivate: [authGuard],
    title: 'Order Tracking | haimuoi2',
  },
  {
    path: 'account/payment-methods',
    component: AccountPaymentMethodsPageComponent,
    canActivate: [authGuard],
    title: 'Payment Methods | haimuoi2',
  },
  {
    path: 'account/wishlist',
    component: AccountWishlistPageComponent,
    canActivate: [authGuard],
    title: 'Wishlist | haimuoi2',
  },
  {
    path: 'admin',
    canActivate: [shopOwnerGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        component: DashboardPageComponent,
        title: 'Dashboard | Admin'
      },
      {
        path: 'inventory',
        component: InventoryPageComponent,
        title: 'Inventory | Admin'
      },
      {
        path: 'orders',
        component: OrdersPageComponent,
        title: 'Orders | Admin'
      },
      {
        path: 'products',
        component: ProductsPageComponent,
        title: 'Products | Admin'
      },
      {
        path: 'categories',
        component: CategoriesPageComponent,
        title: 'Categories | Admin'
      },
      {
        path: 'settings',
        component: SettingsPageComponent,
        title: 'Settings | Admin'
      },
      {
        path: 'orders/:id',
        component: AdminOrderDetailPageComponent,
        title: 'Order Detail | Admin',
      }
    ]
  },
  {
    path: 'sysadmin',
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'access-denied',
        component: SysAdminAccessDeniedComponent,
        title: 'Access Denied | Sysadmin'
      },
      {
        path: 'dashboard',
        component: SysAdminDashboardComponent,
        canActivate: [adminGuard],
        title: 'Tenant Management - Titanium Graphite'
      },
      {
        path: 'users',
        component: SysAdminUsersComponent,
        canActivate: [adminGuard],
        title: 'Titanium Admin | Users & Permissions'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
