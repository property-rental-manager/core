import React from "react";
import { useTranslation } from "react-i18next";
import { Shield } from "lucide-react";
import { useAuth } from "../../auth/useAuth";
import { Card } from "../../components/common/Card";

export const AdminDashboardPage: React.FC = () => {
    const { user } = useAuth();
    const { t } = useTranslation();

    return (
        <div>
            <div style={{ marginBottom: "2rem" }}>
                <h1 style={{ fontSize: "1.75rem", marginBottom: "0.5rem" }}>
                    {t("dashboard.welcome", { name: user?.fullName || "" })}
                </h1>
                <p style={{ color: "var(--text-muted)" }}>
                    {t("dashboard.roleSubtitle", { role: t("roles.ADMIN") })}
                </p>
            </div>

            <Card>
                <div style={{ display: "flex", alignItems: "center", gap: "1rem", color: "var(--primary)" }}>
                    <Shield size={32} />
                    <div>
                        <h3 style={{ margin: 0 }}>{t("roles.ADMIN")}</h3>
                        <p style={{ color: "var(--text-muted)", marginTop: "0.25rem", fontSize: "0.95rem" }}>
                            {t("dashboard.adminNotice")}
                        </p>
                    </div>
                </div>
            </Card>
        </div>
    );
};
