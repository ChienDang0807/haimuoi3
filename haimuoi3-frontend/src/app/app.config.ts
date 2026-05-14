import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideEchartsCore } from 'ngx-echarts';
import { MatDialogModule } from '@angular/material/dialog';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { CartTokenInterceptor } from './core/interceptors/cart-token.interceptor';
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes), 
    provideClientHydration(withEventReplay()),
    // Critical: enable DI-based interceptors (HTTP_INTERCEPTORS) for provideHttpClient API.
    // Without this, CartTokenInterceptor won't run, so X-Cart-Token header is missing.
    provideHttpClient(withFetch(), withInterceptorsFromDi()),
    provideAnimations(),
    importProvidersFrom(MatDialogModule),
    provideEchartsCore({ echarts: () => import('echarts') }),
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: CartTokenInterceptor, multi: true },
  ]
};
