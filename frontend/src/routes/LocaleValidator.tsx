import React from "react";
import { Navigate, useParams } from "react-router-dom";

export const LocaleValidator: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { locale } = useParams<{ locale: string }>();
    if (locale !== "pl" && locale !== "en") {
        return <Navigate to="/pl/404" replace />;
    }
    return <>{children}</>;
};
