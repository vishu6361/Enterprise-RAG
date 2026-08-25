import { api } from "./api";
import { ApiResponse, DocumentItem, DocumentPermissionUpdateReq } from "@/types";

export const documentService = {
  getDocuments: async (): Promise<ApiResponse<DocumentItem[]>> => {
    const response = await api.get<ApiResponse<DocumentItem[]>>("/documents");
    return response.data;
  },

  getDocument: async (id: number): Promise<ApiResponse<DocumentItem>> => {
    const response = await api.get<ApiResponse<DocumentItem>>(`/documents/${id}`);
    return response.data;
  },

  uploadDocument: async (file: File): Promise<ApiResponse<DocumentItem>> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api.post<ApiResponse<DocumentItem>>("/documents/upload", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  deleteDocument: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete<ApiResponse<void>>(`/documents/${id}`);
    return response.data;
  },

  updatePermission: async (req: DocumentPermissionUpdateReq): Promise<ApiResponse<void>> => {
    const response = await api.post<ApiResponse<void>>("/documents/permission", req);
    return response.data;
  },
};
