import React, { createContext, useContext, useState, useEffect } from "react";
import { User, Organization } from "@/types";
import { authService, LoginPayload, SignupPayload } from "@/services/authService";
import { userService } from "@/services/userService";

interface AuthContextType {
  user: User | null;
  token: string | null;
  organization: Organization | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginPayload) => Promise<{ success: boolean; message: string }>;
  signup: (payload: SignupPayload) => Promise<{ success: boolean; message: string }>;
  logout: () => void;
  refreshOrganization: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem("user");
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [organization, setOrganization] = useState<Organization | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const fetchOrgDetails = async (orgId: number) => {
    try {
      const res = await userService.getOrganization(orgId);
      if (res.flag && res.data) {
        setOrganization(res.data);
      }
    } catch (e) {
      console.error("Failed to fetch organization details", e);
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      if (token && user?.organizationId) {
        await fetchOrgDetails(user.organizationId);
      }
      setIsLoading(false);
    };
    initAuth();
  }, [token, user?.organizationId]);

  const login = async (credentials: LoginPayload) => {
    try {
      const res = await authService.login(credentials);
      if (res.flag && res.data) {
        const { token: jwt, user: userData } = res.data;
        setToken(jwt);
        setUser(userData);
        localStorage.setItem("token", jwt);
        localStorage.setItem("user", JSON.stringify(userData));

        if (userData.organizationId) {
          await fetchOrgDetails(userData.organizationId);
        }
        return { success: true, message: res.message || "Login successful" };
      }
      return { success: false, message: res.message || "Invalid credentials" };
    } catch (err: any) {
      const errorMsg =
        err.response?.data?.message || err.message || "Failed to communicate with server";
      return { success: false, message: errorMsg };
    }
  };

  const signup = async (payload: SignupPayload) => {
    try {
      const res = await authService.signup(payload);
      if (res.flag && res.data) {
        const { token: jwt, user: userData } = res.data;
        setToken(jwt);
        setUser(userData);
        localStorage.setItem("token", jwt);
        localStorage.setItem("user", JSON.stringify(userData));

        if (userData.organizationId) {
          await fetchOrgDetails(userData.organizationId);
        }
        return { success: true, message: res.message || "Registration successful" };
      }
      return { success: false, message: res.message || "Registration failed" };
    } catch (err: any) {
      const errorMsg =
        err.response?.data?.message || err.message || "Failed to communicate with server";
      return { success: false, message: errorMsg };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setOrganization(null);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    window.location.href = "/login";
  };

  const refreshOrganization = async () => {
    if (user?.organizationId) {
      await fetchOrgDetails(user.organizationId);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        organization,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        signup,
        logout,
        refreshOrganization,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
