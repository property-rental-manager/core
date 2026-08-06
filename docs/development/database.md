# Local Infrastructure & Database Development Guide

This guide describes how to manage the PostgreSQL database, Flyway migrations, Adminer database tool, and IntelliJ IDEA setup for **Property Rental Manager**.

---

## 1. Prerequisites

- **Docker & Docker Compose** (Docker Desktop or Docker Engine with Compose V2)
- **Java 25 JDK** (configured in your IDE and environment)
- **Maven** (or Maven Wrapper `./mvnw` included in `core/backend`)

---

## 2. Environment Setup

Copy `.env.example` to `.env` in the repository root (`core/`):

```bash
cp .env.example .env
```

> [!IMPORTANT]
> Never commit `.env` to Git. Real secrets belong only in unversioned local `.env` files or deployment environment secret managers.

Key PostgreSQL and Spring Boot environment variables:

| Variable | Default Value | Description |
|---|---|---|
| `POSTGRES_IMAGE` | `postgres:17-alpine` | PostgreSQL Docker image |
| `POSTGRES_DB` | `property_rental_manager` | Database name |
| `POSTGRES_USER` | `property_rental_manager` | Database user |
| `POSTGRES_PASSWORD` | `local_development_password` | Database password |
| `POSTGRES_PORT` | `5432` | Mapped host port for database |
| `ADMINER_PORT` | `8081` | Mapped host port for Adminer |
| `SPRING_PROFILES_ACTIVE` | `development` | Active Spring profile |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/property_rental_manager` | Backend database JDBC URL |

---

## 3. Database Management Scripts

All scripts are located in `core/scripts/` and should be executed from the repository root `core/`:

### Start Database
```bash
./scripts/dev-db-up.sh
```
Starts PostgreSQL in Docker, waits until the healthcheck reaches state `healthy`, and displays connection details.

### Stop Database
```bash
./scripts/dev-db-down.sh
```
Stops the PostgreSQL container without deleting persistent database volume (`postgres_data`).

### View Logs
```bash
./scripts/dev-db-logs.sh
```
Streams realtime logs from the `db` service container.

### Reset Database
```bash
./scripts/dev-db-reset.sh
```
Prompts for explicit confirmation, stops containers, removes the project-specific PostgreSQL dev volume, and starts a fresh database container.

---

## 4. Adminer (Optional Web UI)

Adminer is configured as an optional service under the `--profile tools` Docker Compose profile.

To start Adminer alongside PostgreSQL:
```bash
docker compose --env-file .env -f infra/docker/compose.dev.yaml --profile tools up -d
```

Open Adminer in your browser:
- **URL:** [http://localhost:8081](http://localhost:8081)
- **System:** PostgreSQL
- **Server:** `db` (or `localhost` if connecting outside Docker)
- **Username:** `property_rental_manager`
- **Password:** `local_development_password` (or your `.env` value)
- **Database:** `property_rental_manager`

---

## 5. Database Connection Details

When connecting from host applications (e.g. backend running directly in IntelliJ):

- **Host:** `localhost`
- **Port:** `5432` (or value of `POSTGRES_PORT`)
- **Database:** `property_rental_manager`
- **User:** `property_rental_manager`
- **Password:** (as configured in `.env`)

> [!NOTE]
> Inside the Docker Compose network (e.g. between Adminer and PostgreSQL), the database hostname is `db`.

---

## 6. Flyway Migrations & Schema Policy

### "Flyway Owns the Schema"

1. **Single Source of Truth:** All database schema changes **MUST** be made through versioned SQL migration scripts located in `core/backend/src/main/resources/db/migration/`.
2. **Naming Convention:** `V<version>__<description>.sql` (e.g., `V1__create_identity_tables.sql`).
3. **No Automatic JPA Schema Generation:** `spring.jpa.hibernate.ddl-auto` is set to `validate`. Hibernate validates that entity mappings match the DB schema, but will never create or alter tables automatically (`create`, `create-drop`, and `update` are strictly prohibited).
4. **Manual DDL Prohibited:** Never execute manual `CREATE TABLE` or `ALTER TABLE` statements directly in the database without creating a Flyway migration script.

---

## 7. Running Backend in IntelliJ IDEA

Follow these steps to run the Spring Boot backend during development:

1. **Start PostgreSQL:**
   ```bash
   ./scripts/dev-db-up.sh
   ```
2. **Open Project:** Open `core/` root in IntelliJ IDEA.
3. **Import Maven:** Ensure `core/backend/pom.xml` is imported as a Maven module.
4. **JDK Selection:** Set Project SDK and Module SDK to **Java 25** (matching `pom.xml`).
5. **Run Configuration:**
   - Create a new **Spring Boot** (or Application) run configuration.
   - Main Class: `pl.propertyrentalmanager.PropertyRentalManagerBackendApplication`
   - Active Profiles: `development`
     (or VM Options: `-Dspring.profiles.active=development`)
   - Environment Variables: Load `.env` or set:
     - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/property_rental_manager`
     - `SPRING_DATASOURCE_USERNAME=property_rental_manager`
     - `SPRING_DATASOURCE_PASSWORD=local_development_password`
6. **Verify Flyway in Startup Logs:**
   Look for logs confirming successful migration:
   ```text
   o.f.c.i.s.DefaultFlywayMigrator: Successfully applied 1 migration to schema "public", now at version v1
   ```

---

## 8. Common Troubleshooting

| Issue | Root Cause | Solution |
|---|---|---|
| `Connection refused` | Database container is not running or healthcheck hasn't passed yet. | Run `./scripts/dev-db-up.sh` and verify state using `docker compose -f infra/docker/compose.dev.yaml ps`. |
| `Port 5432 already in use` | Local PostgreSQL instance or another container is running on host port 5432. | Stop host PostgreSQL service or change `POSTGRES_PORT` in `.env`. |
| `Password authentication failed` | Password in `.env` doesn't match credentials initialized in existing volume. | Run `./scripts/dev-db-reset.sh` to recreate database volume with current `.env` credentials. |
| `Flyway checksum mismatch` | A applied Flyway migration file was modified after execution. | Revert edits in the `.sql` file, or run `./scripts/dev-db-reset.sh` on local development environments. |
| `Schema validation failed` | Hibernate entity mappings do not match Flyway database schema. | Verify JPA `@Table` / `@Column` definitions against Flyway SQL migrations. |
