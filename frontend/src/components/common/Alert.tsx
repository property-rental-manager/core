import React from "react";

interface AlertProps {
    type?: "danger" | "success" | "warning";
    message: string;
    className?: string;
}

export const Alert: React.FC<AlertProps> = ({ type = "danger", message, className = "" }) => {
    if (!message) return null;
    return (
        <div className={`alert alert-${type} ${className}`} role="alert">
            <span>{message}</span>
        </div>
    );
};
