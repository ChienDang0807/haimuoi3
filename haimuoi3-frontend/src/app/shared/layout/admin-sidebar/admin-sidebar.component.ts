import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminNavItem } from '../../interfaces/admin';

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-sidebar.component.html',
  styleUrl: './admin-sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminSidebarComponent {
  navItems: AdminNavItem[] = [
    { id: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/admin/dashboard' },
    { id: 'inventory', label: 'Inventory', icon: 'inventory_2', route: '/admin/inventory' },
    { id: 'orders', label: 'Orders', icon: 'shopping_cart', route: '/admin/orders' },
    { id: 'products', label: 'Products', icon: 'inventory', route: '/admin/products' },
    { id: 'categories', label: 'Categories', icon: 'category', route: '/admin/categories' },
    { id: 'settings', label: 'Settings', icon: 'settings', route: '/admin/settings' }
  ];
}
