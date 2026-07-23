import { Link } from "react-router";

export function NotFoundPage() {
    return (
        <main>
            <h1>404</h1>
            <p>Nie znaleziono strony.</p>

            <Link to="/pl/login">Wróć do logowania</Link>
        </main>
    );
}