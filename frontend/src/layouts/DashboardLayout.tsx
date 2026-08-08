import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { NavLink, Outlet, useParams } from "react-router-dom";
import { Building, Home, LogOut, Menu, User as UserIcon, X } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import { LanguageSwitcher } from "../components/common/LanguageSwitcher";
import { ThemeToggle } from "../components/common/ThemeToggle";

export const DashboardLayout: React.FC = () => {
    const { user, logout } = useAuth();
    const { t } = useTranslation();
    const { locale = "pl" } = useParams<{ locale: string }>();
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

    const getPrimaryRole = () => {
        if (!user || !user.roles || user.roles.length === 0) return "";
        return user.roles[0];
    };

    const primaryRole = getPrimaryRole();

    return (
        <div className="dashboard-layout">
            {/* Sidebar */}
            <aside className={`sidebar ${isMobileMenuOpen ? "open" : ""}`}>
                <div className="sidebar-header">
                    <Building size={24} style={{ color: "var(--primary)" }} />
                    <span>{t("common.appName")}</span>
                </div>

                <nav className="sidebar-nav">
                    <NavLink
                        to={`/${locale}/dashboard`}
                        end
                        className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
                        onClick={() => setIsMobileMenuOpen(false)}
                    >
                        <Home size={18} />
                        <span>{t("navigation.dashboard")}</span>
                    </NavLink>

                    <NavLink
                        to={`/${locale}/profile`}
                        className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
                        onClick={() => setIsMobileMenuOpen(false)}
                    >
                        <UserIcon size={18} />
                        <span>{t("navigation.profile")}</span>
                    </NavLink>
                </nav>
            </aside>

            {/* Main Area */}
            <div className="dashboard-main">
                <header className="header">
                    <button
                        type="button"
                        aria-label="Toggle Mobile Menu"
                        className="btn-secondary"
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                        style={{ display: "none", padding: "0.5rem" }} // Shown in CSS media queries if needed
                    >
                        {isMobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
                    </button>

                    <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                        <span className="badge badge-primary">{primaryRole ? t(`roles.${primaryRole}`) : ""}</span>
                        <span style={{ fontWeight: 500, fontSize: "0.9rem" }}>{user?.fullName}</span>
                    </div>

                    <div className="header-controls">
                        <LanguageSwitcher />
                        <ThemeToggle />
                        <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => logout()}
                            title={t("common.logout")}
                            style={{ padding: "0.5rem 0.75rem", fontSize: "0.85rem" }}
                        >
                            <LogOut size={16} />
                            <span>{t("common.logout")}</span>
                        </button>
                    </div>
                </header>

                <main className="page-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};
