export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface CustomerProfile {
  userId: number;
  email: string;
  phone: string | null;
  fullName: string;
  avatarUrl: string | null;
  dateOfBirth: string | null;
  gender: Gender | null;
  isVerified: boolean;
}

export interface UpdateProfileRequest {
  fullName?: string;
  avatarUrl?: string | null;
  dateOfBirth?: string | null;
  gender?: Gender | null;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
}
