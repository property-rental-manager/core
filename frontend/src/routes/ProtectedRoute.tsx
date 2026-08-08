import React from "react";
import { Navigate, Outlet, useLocation, useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { Spinner } from "../components/common/Spinner";

export const ProtectedRoute: React.FC = () => {
    const { isAuthenticated, isInitializing } = useAuth();
    const { locale = "pl" } = useParams<{ locale: string }>();
    const location = useLocation();

    if (isInitializing) {
        return (
            <div className="bootstrap-screen">
                <Spinner variant="primary" size={36} />
                <span style={{ color: "var(--text-muted)", fontSize: "0.9rem" }}>Loading Property Rental Manager...</span>
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to={`/${locale}/login`} state={{ from: location }} replace />;
    }

    return <Outlet />;
};
