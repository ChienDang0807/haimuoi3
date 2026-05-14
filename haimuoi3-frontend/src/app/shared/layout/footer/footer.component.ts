import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  shopLinks = [
    { label: 'Collections', route: '/collections' },
    { label: 'New Arrivals', route: '/new-arrivals' },
    { label: 'Bespoke', route: '/bespoke' }
  ];

  companyLinks = [
    { label: 'About haimuoi2', route: '/about' },
    { label: 'Sustainability', route: '/sustainability' },
    { label: 'Privacy Policy', route: '/privacy' }
  ];

  supportLinks = [
    { label: 'Shipping & Returns', route: '/shipping' },
    { label: 'Contact', route: '/contact' }
  ];

  onSocialClick(platform: string): void {
    console.log('Social platform clicked:', platform);
  }
}
