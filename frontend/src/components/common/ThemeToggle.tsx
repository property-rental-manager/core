import React from "react";
import { Moon, Sun } from "lucide-react";
import { useTheme } from "../../themes/useTheme";

export const ThemeToggle: React.FC = () => {
    const { theme, toggleTheme } = useTheme();

    return (
        <button
            type="button"
            onClick={toggleTheme}
            aria-label="Toggle Theme"
            title={theme === "light" ? "Switch to Dark Mode" : "Switch to Light Mode"}
            style={{
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                padding: "0.5rem",
                borderRadius: "var(--radius-md)",
                border: "1px solid var(--border-color)",
                backgroundColor: "var(--bg-input)",
                color: "var(--text-main)",
                cursor: "pointer",
            }}
        >
            {theme === "light" ? <Moon size={18} /> : <Sun size={18} />}
        </button>
    );
};
