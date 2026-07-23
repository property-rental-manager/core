import { useTranslation } from "react-i18next";

export function LoginPage() {
    const { t } = useTranslation();

    return (
        <main>
            <h1>{t("app.name")}</h1>
            <h2>{t("auth.login")}</h2>

            <p>Frontend został poprawnie skonfigurowany.</p>
        </main>
    );
}