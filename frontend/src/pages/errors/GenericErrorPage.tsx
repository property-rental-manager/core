import React, { Component } from "react";
import type { ErrorInfo, ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { AlertTriangle } from "lucide-react";
import { Button } from "../../components/common/Button";

interface Props {
    children: ReactNode;
}

interface State {
    hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
    public state: State = {
        hasError: false,
    };

    public static getDerivedStateFromError(): State {
        return { hasError: true };
    }

    public componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
        // Log to console in dev only, without exposing tokens
        if (import.meta.env.DEV) {
            console.error("Uncaught error:", error, errorInfo);
        }
    }

    public render(): ReactNode {
        if (this.state.hasError) {
            return <GenericErrorPage onRetry={() => this.setState({ hasError: false })} />;
        }
        return this.props.children;
    }
}

export const GenericErrorPage: React.FC<{ onRetry?: () => void }> = ({ onRetry }) => {
    const { t } = useTranslation();

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
                    <AlertTriangle size={48} />
                </div>
                <h1 style={{ fontSize: "1.5rem", marginBottom: "0.5rem" }}>{t("errors.genericTitle")}</h1>
                <p style={{ color: "var(--text-muted)", marginBottom: "1.5rem" }}>{t("errors.genericMessage")}</p>
                <Button
                    variant="primary"
                    onClick={() => {
                        if (onRetry) {
                            onRetry();
                        } else {
                            window.location.reload();
                        }
                    }}
                >
                    {t("errors.retry")}
                </Button>
            </div>
        </div>
    );
};
