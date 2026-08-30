import { api } from "./api";
import { ApiResponse, User, Organization, UserDesignation } from "@/types";

export interface CreateUserPayload {
  name: string;
  email: string;
  password: string;
  designation: UserDesignation;
  organizationId?: number;
}

export const userService = {
  getUsers: async (): Promise<ApiResponse<User[]>> => {
    const response = await api.get<ApiResponse<User[]>>("/users");
    return response.data;
  },

  getUser: async (id: number): Promise<ApiResponse<User>> => {
    const response = await api.get<ApiResponse<User>>(`/users/${id}`);
    return response.data;
  },

  createUser: async (payload: CreateUserPayload): Promise<ApiResponse<User>> => {
    const response = await api.post<ApiResponse<User>>("/users", payload);
    return response.data;
  },

  deleteUser: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete<ApiResponse<void>>(`/users/${id}`);
    return response.data;
  },

  getOrganization: async (id: number): Promise<ApiResponse<Organization>> => {
    const response = await api.get<ApiResponse<Organization>>(`/organizations/${id}`);
    return response.data;
  },
};
