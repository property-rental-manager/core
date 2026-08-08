import React from "react";
import { Navigate, Outlet, useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import type { Role } from "../types/auth";

interface RoleRouteProps {
    allowedRoles: Role[];
}

export const RoleRoute: React.FC<RoleRouteProps> = ({ allowedRoles }) => {
    const { user } = useAuth();
    const { locale = "pl" } = useParams<{ locale: string }>();

    if (!user || !user.roles) {
        return <Navigate to={`/${locale}/403`} replace />;
    }

    const hasAllowedRole = user.roles.some((r) => allowedRoles.includes(r));

    if (!hasAllowedRole) {
        return <Navigate to={`/${locale}/403`} replace />;
    }

    return <Outlet />;
};
