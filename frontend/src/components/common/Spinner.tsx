import React from "react";

interface SpinnerProps {
    variant?: "white" | "primary";
    size?: number;
}

export const Spinner: React.FC<SpinnerProps> = ({ variant = "white", size = 24 }) => {
    const className = variant === "primary" ? "spinner spinner-primary" : "spinner";
    return <div className={className} style={{ width: size, height: size }} role="status" aria-label="loading" />;
};
