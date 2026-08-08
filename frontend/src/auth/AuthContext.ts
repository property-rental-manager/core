import { createContext } from "react";
import type { AuthStatus, ChangePasswordRequest, LoginRequest, User } from "../types/auth";

export interface AuthContextType {
    user: User | null;
    accessToken: string | null;
    status: AuthStatus;
    isAuthenticated: boolean;
    isInitializing: boolean;
    login: (credentials: LoginRequest) => Promise<User>;
    logout: () => Promise<void>;
    refreshSession: () => Promise<boolean>;
    changePassword: (dto: ChangePasswordRequest) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
