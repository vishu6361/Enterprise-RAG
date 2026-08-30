import { api } from "./api";
import { ApiResponse, AuthResponse } from "@/types";

export interface LoginPayload {
  email: string;
  password: string;
}

export interface SignupPayload {
  organizationName: string;
  name: string;
  email: string;
  password: string;
  contactPhone?: string;
}

export const authService = {
  login: async (credentials: LoginPayload): Promise<ApiResponse<AuthResponse>> => {
    const response = await api.post<ApiResponse<AuthResponse>>("/auth/login", credentials);
    return response.data;
  },

  signup: async (payload: SignupPayload): Promise<ApiResponse<AuthResponse>> => {
    const response = await api.post<ApiResponse<AuthResponse>>("/auth/signup", payload);
    return response.data;
  },
};
