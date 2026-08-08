import React from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { Globe } from "lucide-react";
import type { SupportedLocale } from "../../types/auth";

export const LanguageSwitcher: React.FC = () => {
    const { i18n } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const params = useParams<{ locale?: string }>();

    const currentLocale = (params.locale as SupportedLocale) || (i18n.language as SupportedLocale) || "pl";

    const changeLanguage = (newLocale: SupportedLocale) => {
        i18n.changeLanguage(newLocale);
        if (params.locale && params.locale !== newLocale) {
            const newPath = location.pathname.replace(`/${params.locale}`, `/${newLocale}`);
            navigate(newPath, { replace: true });
        }
    };

    return (
        <div style={{ display: "inline-flex", alignItems: "center", gap: "0.5rem" }}>
            <Globe size={18} style={{ color: "var(--text-muted)" }} />
            <select
                aria-label="Select Language"
                value={currentLocale}
                onChange={(e) => changeLanguage(e.target.value as SupportedLocale)}
                style={{
                    padding: "0.375rem 0.625rem",
                    borderRadius: "var(--radius-sm)",
                    border: "1px solid var(--border-color)",
                    backgroundColor: "var(--bg-input)",
                    color: "var(--text-main)",
                    cursor: "pointer",
                }}
            >
                <option value="pl">Polski (PL)</option>
                <option value="en">English (EN)</option>
            </select>
        </div>
    );
};
