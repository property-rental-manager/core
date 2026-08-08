import React from "react";

interface FormFieldProps {
    label: string;
    htmlFor?: string;
    error?: string;
    children: React.ReactNode;
}

export const FormField: React.FC<FormFieldProps> = ({
    label,
    htmlFor,
    error,
    children,
}) => {
    return (
        <div className="form-group">
            <label className="form-label" htmlFor={htmlFor}>
                {label}
            </label>
            {children}
            {error && (
                <span className="text-danger" style={{ fontSize: "0.8rem", marginTop: "0.25rem", color: "var(--danger)" }}>
                    {error}
                </span>
            )}
        </div>
    );
};
