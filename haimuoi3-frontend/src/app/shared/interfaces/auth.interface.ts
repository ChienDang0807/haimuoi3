export interface LoginRequest {
  emailOrPhone: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  phone?: string;
  password: string;
  passwordConfirm: string;
  fullName: string;
}

export interface AuthResponse {
  accessToken: string;
  userId: number;
  email: string;
  role: string;
  expiresIn: number;
}

export interface UserInfo {
  userId: number;
  email: string;
  fullName: string;
  role: string;
  shopId?: number;
}
