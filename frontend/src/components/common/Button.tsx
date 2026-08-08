import React from "react";
import type { ButtonHTMLAttributes } from "react";
import { Spinner } from "./Spinner";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: "primary" | "secondary" | "danger";
    isLoading?: boolean;
    children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
    variant = "primary",
    isLoading = false,
    disabled,
    children,
    className = "",
    ...props
}) => {
    return (
        <button
            className={`btn btn-${variant} ${className}`}
            disabled={disabled || isLoading}
            {...props}
        >
            {isLoading ? <Spinner variant={variant === "secondary" ? "primary" : "white"} size={18} /> : children}
        </button>
    );
};
