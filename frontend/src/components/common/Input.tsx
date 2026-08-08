import { forwardRef } from "react";
import type { InputHTMLAttributes } from "react";

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    hasError?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
    ({ hasError, className = "", ...props }, ref) => {
        const errorClass = hasError ? "border-danger" : "";
        return (
            <input
                ref={ref}
                className={`form-input ${errorClass} ${className}`}
                {...props}
            />
        );
    },
);

Input.displayName = "Input";
