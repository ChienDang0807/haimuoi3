import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HeroSlide } from '../../shared/interfaces';

@Injectable({
  providedIn: 'root'
})
export class HeroService {
  getHeroSlides(): Observable<HeroSlide[]> {
    const slides: HeroSlide[] = [
      {
        id: '1',
        title: 'Art of<br />Living',
        subtitle: 'Precision Redefined',
        description: 'Experience technical precision in your daily rituals. Our curated home appliance series merges industrial engineering with domestic elegance.',
        imageUrl: 'assets/images/hero-1.png',
        startingPrice: 1499.00,
        ctaText: 'Shop Now',
        ctaRoute: '/shop'
      },
      {
        id: '2',
        title: 'Premium<br />Collection',
        subtitle: 'Excellence Delivered',
        description: 'Discover our meticulously crafted collection of premium home appliances designed for the discerning individual.',
        imageUrl: 'assets/images/hero-2.png',
        startingPrice: 999.00,
        ctaText: 'Explore',
        ctaRoute: '/collections'
      },
      {
        id: '3',
        title: 'Modern<br />Design',
        subtitle: 'Form Meets Function',
        description: 'Where minimalist aesthetics converge with maximum functionality. Elevate your living space.',
        imageUrl: 'assets/images/hero-3.png',
        startingPrice: 799.00,
        ctaText: 'Discover',
        ctaRoute: '/new-arrivals'
      }
    ];

    return of(slides);
  }
}
