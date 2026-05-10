import { api } from './axios';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/api';

export const authApi = {
  login: (body: LoginRequest) =>
    api.post<AuthResponse>('/auth/login', body).then((r) => r.data),
  register: (body: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', body).then((r) => r.data),
};
