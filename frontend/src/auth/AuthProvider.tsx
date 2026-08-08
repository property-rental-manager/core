import React, { useCallback, useEffect, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { authApi } from "../api/authApi";
import { clearMemoryToken, setMemoryToken, setOnUnauthenticatedCallback } from "../api/apiClient";
import { AuthContext } from "./AuthContext";
import type { AuthStatus, ChangePasswordRequest, LoginRequest, User } from "../types/auth";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [accessToken, setAccessTokenState] = useState<string | null>(null);
    const [status, setStatus] = useState<AuthStatus>("INITIALIZING");
    const queryClient = useQueryClient();

    const updateAuthMemory = (token: string | null, userData: User | null) => {
        setMemoryToken(token);
        setAccessTokenState(token);
        setUser(userData);
    };

    const handleLocalLogout = useCallback(() => {
        clearMemoryToken();
        setAccessTokenState(null);
        setUser(null);
        setStatus("UNAUTHENTICATED");
        queryClient.clear();
    }, [queryClient]);

    useEffect(() => {
        setOnUnauthenticatedCallback(() => {
            handleLocalLogout();
        });
    }, [handleLocalLogout]);

    const refreshSession = useCallback(async (): Promise<boolean> => {
        try {
            await authApi.fetchCsrf();
            const res = await authApi.refresh();
            updateAuthMemory(res.accessToken, res.user);
            setStatus("AUTHENTICATED");
            return true;
        } catch {
            handleLocalLogout();
            return false;
        }
    }, [handleLocalLogout]);

    useEffect(() => {
        let isMounted = true;

        const bootstrap = async () => {
            try {
                await authApi.fetchCsrf();
                const res = await authApi.refresh();
                if (isMounted) {
                    updateAuthMemory(res.accessToken, res.user);
                    setStatus("AUTHENTICATED");
                }
            } catch {
                if (isMounted) {
                    handleLocalLogout();
                }
            }
        };

        bootstrap();

        return () => {
            isMounted = false;
        };
    }, [handleLocalLogout]);

    const login = async (credentials: LoginRequest): Promise<User> => {
        await authApi.fetchCsrf();
        const res = await authApi.login(credentials);
        updateAuthMemory(res.accessToken, res.user);
        setStatus("AUTHENTICATED");
        return res.user;
    };

    const logout = async (): Promise<void> => {
        try {
            await authApi.logout();
        } catch {
            // Ignore network errors on logout, proceed with local logout
        } finally {
            handleLocalLogout();
        }
    };

    const changePassword = async (dto: ChangePasswordRequest): Promise<void> => {
        await authApi.changePassword(dto);
        handleLocalLogout();
    };

    const isAuthenticated = status === "AUTHENTICATED" && user !== null;
    const isInitializing = status === "INITIALIZING";

    return (
        <AuthContext.Provider
            value={{
                user,
                accessToken,
                status,
                isAuthenticated,
                isInitializing,
                login,
                logout,
                refreshSession,
                changePassword,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};
