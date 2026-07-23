import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import "./i18n";
import "./index.css";

import { App } from "./app/App";
import { AppProviders } from "./app/AppProviders";

const rootElement = document.getElementById("root");

if (!rootElement) {
    throw new Error("Root element was not found");
}

createRoot(rootElement).render(
    <StrictMode>
        <AppProviders>
            <App />
        </AppProviders>
    </StrictMode>,
);