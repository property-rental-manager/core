import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { KeyRound } from "lucide-react";
import { useAuth } from "../../auth/useAuth";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Card } from "../../components/common/Card";
import { FormField } from "../../components/common/FormField";
import { Input } from "../../components/common/Input";
import { ApiError } from "../../api/apiClient";

const changePasswordSchema = z
    .object({
        currentPassword: z.string().min(1, "Current password is required"),
        newPassword: z
            .string()
            .min(12, "New password must be at least 12 characters")
            .max(128, "New password must not exceed 128 characters"),
        confirmNewPassword: z.string().min(1, "Confirm password is required"),
    })
    .refine((data) => data.newPassword === data.confirmNewPassword, {
        message: "New passwords do not match",
        path: ["confirmNewPassword"],
    })
    .refine((data) => data.currentPassword !== data.newPassword, {
        message: "New password must differ from current password",
        path: ["newPassword"],
    });

type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;

export const ProfilePage: React.FC = () => {
    const { user, changePassword } = useAuth();
    const { t } = useTranslation();

    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<ChangePasswordFormData>({
        resolver: zodResolver(changePasswordSchema),
    });

    const onSubmit = async (data: ChangePasswordFormData) => {
        setErrorMessage(null);
        setSuccessMessage(null);
        setIsLoading(true);

        try {
            await changePassword({
                currentPassword: data.currentPassword,
                newPassword: data.newPassword,
            });
            setSuccessMessage(t("profile.passwordChangedSuccess"));
            reset();
        } catch (err) {
            if (err instanceof ApiError) {
                if (err.code === "PASSWORD_POLICY_VIOLATION") {
                    setErrorMessage(t("profile.passwordPolicyError"));
                } else {
                    setErrorMessage(err.message || "Failed to change password");
                }
            } else {
                setErrorMessage("Failed to change password. Connection error.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: "800px" }}>
            <h1 style={{ fontSize: "1.75rem", marginBottom: "1.5rem" }}>{t("profile.title")}</h1>

            {/* Account Details Card */}
            <Card title={t("profile.accountDetails")}>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1.25rem" }}>
                    <div>
                        <span style={{ fontSize: "0.85rem", color: "var(--text-muted)", display: "block" }}>
                            {t("profile.fullName")}
                        </span>
                        <span style={{ fontWeight: 600, fontSize: "1.05rem" }}>{user?.fullName}</span>
                    </div>

                    <div>
                        <span style={{ fontSize: "0.85rem", color: "var(--text-muted)", display: "block" }}>
                            {t("profile.email")}
                        </span>
                        <span style={{ fontWeight: 600, fontSize: "1.05rem" }}>{user?.email}</span>
                    </div>

                    <div>
                        <span style={{ fontSize: "0.85rem", color: "var(--text-muted)", display: "block" }}>
                            {t("profile.status")}
                        </span>
                        <span className="badge badge-success" style={{ marginTop: "0.25rem" }}>
                            {user?.status ? t(`userStatus.${user.status}`) : ""}
                        </span>
                    </div>

                    <div>
                        <span style={{ fontSize: "0.85rem", color: "var(--text-muted)", display: "block" }}>
                            {t("profile.roles")}
                        </span>
                        <div style={{ display: "flex", gap: "0.375rem", marginTop: "0.25rem" }}>
                            {user?.roles?.map((r) => (
                                <span key={r} className="badge badge-primary">
                                    {t(`roles.${r}`)}
                                </span>
                            ))}
                        </div>
                    </div>
                </div>
            </Card>

            {/* Change Password Card */}
            <Card title={t("profile.changePasswordTitle")}>
                {errorMessage && <Alert type="danger" message={errorMessage} />}
                {successMessage && <Alert type="success" message={successMessage} />}

                <form onSubmit={handleSubmit(onSubmit)} noValidate style={{ maxWidth: "500px" }}>
                    <FormField label={t("profile.currentPassword")} htmlFor="currentPassword" error={errors.currentPassword?.message}>
                        <Input
                            id="currentPassword"
                            type="password"
                            hasError={!!errors.currentPassword}
                            {...register("currentPassword")}
                        />
                    </FormField>

                    <FormField label={t("profile.newPassword")} htmlFor="newPassword" error={errors.newPassword?.message}>
                        <Input
                            id="newPassword"
                            type="password"
                            hasError={!!errors.newPassword}
                            {...register("newPassword")}
                        />
                    </FormField>

                    <FormField label={t("profile.confirmNewPassword")} htmlFor="confirmNewPassword" error={errors.confirmNewPassword?.message}>
                        <Input
                            id="confirmNewPassword"
                            type="password"
                            hasError={!!errors.confirmNewPassword}
                            {...register("confirmNewPassword")}
                        />
                    </FormField>

                    <Button type="submit" variant="primary" isLoading={isLoading} style={{ marginTop: "0.5rem" }}>
                        <KeyRound size={18} />
                        <span>{t("profile.changePasswordButton")}</span>
                    </Button>
                </form>
            </Card>
        </div>
    );
};
