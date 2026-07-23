import { Navigate, Route, Routes } from "react-router";

import { LoginPage } from "../pages/LoginPage";
import { NotFoundPage } from "../pages/NotFoundPage";

export function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/pl/login" replace />} />

            <Route path="/:locale/login" element={<LoginPage />} />

            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}