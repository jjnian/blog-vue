import { apiGet, apiPost } from './http';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  nickname?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: number;
    username: string;
    nickname?: string;
    avatar?: string;
    email?: string;
    roles: string[];
  };
}

export const login = (data: LoginRequest) =>
  apiPost<AuthResponse, LoginRequest>('/auth/login', data);

export const register = (data: RegisterRequest) =>
  apiPost<AuthResponse, RegisterRequest>('/auth/register', data);

export const getCurrentUser = () =>
  apiGet<AuthResponse['user']>('/auth/me');
