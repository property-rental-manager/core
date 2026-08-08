import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../auth/useAuth";
import type { Role } from "../../types/auth";
import { AdminDashboardPage } from "./AdminDashboardPage";
import { OwnerDashboardPage } from "./OwnerDashboardPage";
import { TenantDashboardPage } from "./TenantDashboardPage";

export const DashboardDispatcherPage: React.FC = () => {
    const { user } = useAuth();
    const { t } = useTranslation();

    const roles: Role[] = user?.roles || [];

    const [selectedRole, setSelectedRole] = useState<Role>(() => {
        if (roles.includes("ADMIN")) return "ADMIN";
        if (roles.includes("OWNER")) return "OWNER";
        if (roles.includes("TENANT")) return "TENANT";
        return "TENANT";
    });

    if (roles.length <= 1) {
        const primaryRole = roles[0] || "TENANT";
        if (primaryRole === "ADMIN") return <AdminDashboardPage />;
        if (primaryRole === "OWNER") return <OwnerDashboardPage />;
        return <TenantDashboardPage />;
    }

    return (
        <div>
            <div
                style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "0.5rem",
                    marginBottom: "1.5rem",
                    borderBottom: "1px solid var(--border-color)",
                    paddingBottom: "0.75rem",
                }}
            >
                <span style={{ fontSize: "0.9rem", color: "var(--text-muted)", fontWeight: 500 }}>
                    Select Context:
                </span>
                {roles.map((r) => (
                    <button
                        key={r}
                        type="button"
                        className={`btn ${selectedRole === r ? "btn-primary" : "btn-secondary"}`}
                        onClick={() => setSelectedRole(r)}
                        style={{ padding: "0.375rem 0.875rem", fontSize: "0.85rem" }}
                    >
                        {t(`roles.${r}`)}
                    </button>
                ))}
            </div>

            {selectedRole === "ADMIN" && <AdminDashboardPage />}
            {selectedRole === "OWNER" && <OwnerDashboardPage />}
            {selectedRole === "TENANT" && <TenantDashboardPage />}
        </div>
    );
};
