export type UserDesignation = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export type DocumentStatus = 'PROCESSING' | 'COMPLETED' | 'FAILED';

export type DocumentPermissionType = 'VIEWER' | 'EDITOR' | 'OWNER' | 'ADMIN';

export interface User {
  id: number;
  name: string;
  email: string;
  organizationId: number;
  designation: UserDesignation;
}

export interface Organization {
  id: number;
  name: string;
  contactEmail: string;
  contactPhone?: string;
  address?: string;
}

export interface DocumentItem {
  id: number;
  documentName: string;
  contentHash: string;
  extension: string;
  status: DocumentStatus;
  organizationId: number;
  ownerId: number;
  ownerName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentPermissionUpdateReq {
  documentId: number;
  userId: number;
  permissionType: DocumentPermissionType;
}

export interface ApiResponse<T> {
  flag: boolean;
  message: string;
  data: T;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}
