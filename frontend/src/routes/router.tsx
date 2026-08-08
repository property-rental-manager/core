import { createBrowserRouter, Navigate } from "react-router-dom";
import { AuthLayout } from "../layouts/AuthLayout";
import { DashboardLayout } from "../layouts/DashboardLayout";
import { LoginPage } from "../pages/auth/LoginPage";
import { DashboardDispatcherPage } from "../pages/dashboard/DashboardDispatcherPage";
import { ProfilePage } from "../pages/profile/ProfilePage";
import { ForbiddenPage } from "../pages/errors/ForbiddenPage";
import { NotFoundPage } from "../pages/errors/NotFoundPage";
import { ProtectedRoute } from "./ProtectedRoute";
import { LocaleValidator } from "./LocaleValidator";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <Navigate to="/pl/dashboard" replace />,
    },
    {
        path: "/:locale",
        element: (
            <LocaleValidator>
                <AuthLayout />
            </LocaleValidator>
        ),
        children: [
            {
                path: "login",
                element: <LoginPage />,
            },
            {
                path: "403",
                element: <ForbiddenPage />,
            },
            {
                path: "404",
                element: <NotFoundPage />,
            },
        ],
    },
    {
        path: "/:locale",
        element: (
            <LocaleValidator>
                <ProtectedRoute />
            </LocaleValidator>
        ),
        children: [
            {
                element: <DashboardLayout />,
                children: [
                    {
                        path: "dashboard",
                        element: <DashboardDispatcherPage />,
                    },
                    {
                        path: "profile",
                        element: <ProfilePage />,
                    },
                ],
            },
        ],
    },
    {
        path: "*",
        element: <Navigate to="/pl/404" replace />,
    },
]);
