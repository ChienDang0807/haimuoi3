import { Component, computed, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPageComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    password: ['', [Validators.required, Validators.minLength(6)]],
    passwordConfirm: ['', [Validators.required]],
  });

  readonly passwordMismatch = computed(() => {
    const value = this.form.getRawValue();
    return value.passwordConfirm.length > 0 && value.password !== value.passwordConfirm;
  });

  onSubmit(): void {
    if (this.form.invalid || this.passwordMismatch() || this.isLoading()) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const phoneTrimmed = raw.phone.trim();
    const password = raw.password.trimEnd();
    this.authService
      .register({
        fullName: raw.fullName.trim(),
        email: raw.email.trim(),
        phone: phoneTrimmed ? phoneTrimmed : undefined,
        password,
        passwordConfirm: raw.passwordConfirm.trimEnd(),
      })
      .subscribe({
        next: () => this.router.navigate(['/']),
        error: (err) => {
          this.errorMessage.set(err.error?.message ?? 'Registration failed. Please try again.');
          this.isLoading.set(false);
        },
      });
  }
}
