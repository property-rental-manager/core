import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";
import { ShieldAlert } from "lucide-react";

export const ForbiddenPage: React.FC = () => {
    const { t } = useTranslation();
    const { locale = "pl" } = useParams<{ locale: string }>();

    return (
        <div className="auth-layout">
            <div className="auth-card" style={{ textAlign: "center" }}>
                <div
                    style={{
                        display: "inline-flex",
                        padding: "1rem",
                        borderRadius: "50%",
                        backgroundColor: "var(--danger-light)",
                        color: "var(--danger)",
                        marginBottom: "1rem",
                    }}
                >
                    <ShieldAlert size={48} />
                </div>
                <h1 style={{ fontSize: "1.5rem", marginBottom: "0.5rem" }}>{t("errors.403Title")}</h1>
                <p style={{ color: "var(--text-muted)", marginBottom: "1.5rem" }}>{t("errors.403Message")}</p>
                <Link to={`/${locale}/dashboard`} className="btn btn-primary" style={{ display: "inline-flex" }}>
                    {t("common.backToHome")}
                </Link>
            </div>
        </div>
    );
};
