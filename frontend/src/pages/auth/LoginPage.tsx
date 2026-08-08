import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Building, Lock } from "lucide-react";
import { useAuth } from "../../auth/useAuth";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { FormField } from "../../components/common/FormField";
import { Input } from "../../components/common/Input";
import { LanguageSwitcher } from "../../components/common/LanguageSwitcher";
import { ThemeToggle } from "../../components/common/ThemeToggle";
import { ApiError } from "../../api/apiClient";

const loginSchema = z.object({
    email: z.string().min(1, "Email is required").email("Invalid email format").max(255),
    password: z.string().min(1, "Password is required"),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
    const { login } = useAuth();
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { locale = "pl" } = useParams<{ locale: string }>();

    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (data: LoginFormData) => {
        setErrorMessage(null);
        setIsLoading(true);

        try {
            await login(data);
            navigate(`/${locale}/dashboard`, { replace: true });
        } catch (err) {
            if (err instanceof ApiError) {
                if (err.code === "RATE_LIMIT_EXCEEDED") {
                    setErrorMessage(t("auth.rateLimitError"));
                } else if (err.code === "INVALID_CREDENTIALS") {
                    setErrorMessage(t("auth.loginFailed"));
                } else {
                    setErrorMessage(err.message || t("auth.loginFailed"));
                }
            } else {
                setErrorMessage(t("auth.networkError"));
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div>
            <div style={{ textAlign: "center", marginBottom: "2rem" }}>
                <div
                    style={{
                        display: "inline-flex",
                        padding: "0.75rem",
                        borderRadius: "var(--radius-lg)",
                        backgroundColor: "var(--primary-light)",
                        color: "var(--primary)",
                        marginBottom: "1rem",
                    }}
                >
                    <Building size={32} />
                </div>
                <h1 style={{ fontSize: "1.5rem", marginBottom: "0.5rem" }}>{t("auth.loginTitle")}</h1>
                <p style={{ color: "var(--text-muted)", fontSize: "0.9rem" }}>{t("auth.loginSubtitle")}</p>
            </div>

            {errorMessage && <Alert type="danger" message={errorMessage} />}

            <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <FormField label={t("auth.emailLabel")} htmlFor="email" error={errors.email?.message}>
                    <Input
                        id="email"
                        type="email"
                        placeholder={t("auth.emailPlaceholder")}
                        hasError={!!errors.email}
                        {...register("email")}
                    />
                </FormField>

                <FormField label={t("auth.passwordLabel")} htmlFor="password" error={errors.password?.message}>
                    <Input
                        id="password"
                        type="password"
                        placeholder={t("auth.passwordPlaceholder")}
                        hasError={!!errors.password}
                        {...register("password")}
                    />
                </FormField>

                <Button type="submit" variant="primary" isLoading={isLoading} style={{ width: "100%", marginTop: "1rem" }}>
                    <Lock size={18} />
                    <span>{t("auth.loginButton")}</span>
                </Button>
            </form>

            <div
                style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    marginTop: "2rem",
                    paddingTop: "1rem",
                    borderTop: "1px solid var(--border-color)",
                }}
            >
                <LanguageSwitcher />
                <ThemeToggle />
            </div>
        </div>
    );
};
