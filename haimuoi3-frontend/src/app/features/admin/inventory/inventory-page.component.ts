import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { AlertCardComponent } from '../../../shared/components/alert-card/alert-card.component';

@Component({
  selector: 'app-inventory-page',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent, AdminHeaderComponent, AlertCardComponent],
  templateUrl: './inventory-page.component.html',
  styleUrl: './inventory-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InventoryPageComponent {
  
}
