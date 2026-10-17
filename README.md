# RideAlongUG

A car rental platform for Uganda - customers can browse and book cars, trucks and tractors, owners can list their own vehicles, and admins handle verification, payouts and fleet oversight. Built as an internship project.

## Structure

- `backend/` - Spring Boot API (Java, Spring Boot 4.1.0)
- `frontend/` - Angular app (Angular 19)

## Running it locally

You'll need Java 21, Maven, Node, and a Postgres instance running somewhere.

### 1. Database

Create a Postgres database for the project. Any name works, just make sure it matches what you put in the properties file in the next step.

### 2. Backend

Inside `backend/src/main/resources/`, copy `application.properties.example` to a new file called `application.properties` and fill in your own values - DB URL, username, password, the JWT secret, and mail credentials if you're working with the email feature. The secret has to be base64, so generate one with `openssl rand -base64 64`. This file is gitignored on purpose, so your real values never end up in the repo.

Once that's set, run the backend from IntelliJ or from the terminal:

```
mvn spring-boot:run
```

Get this running first and confirm it starts cleanly before moving on to the frontend - the Angular app talks to it directly and won't have much to show otherwise.

### 3. Frontend

```
cd frontend
npm install
```

On some machines `npm install` blocks a few install scripts (esbuild being the usual one) and things won't run properly afterward. If `npm start` complains or the dev server behaves oddly, run this once:

```
node node_modules/esbuild/install.js
```

then start the dev server:

```
npm start
```

Open `http://localhost:5173` in your browser once it's compiled.

The frontend talks to `localhost:8080` when you run it locally and to the hosted backend otherwise. Both URLs are at the top of `frontend/src/app/core/api.service.ts` if you need to change either.

## Branching

Branch off `main`: `feature/<area>-<short-description>`. Open a PR into `main` rather than pushing directly, especially now that there's more than one of us on this.

## Deploying

The free setup we use: Neon for Postgres, Render for the backend (it builds from `backend/Dockerfile`, with Root Directory set to `backend`) and Vercel for the frontend (Root Directory `frontend`, output directory `dist/ridealongug-frontend/browser`).

Render has no `application.properties` to read, so every value from the example file goes into its Environment tab as an environment variable instead, written in upper case with underscores (`spring.datasource.url` becomes `SPRING_DATASOURCE_URL`). The ones that aren't in the example but are still required are `SPRING_JPA_HIBERNATE_DDL_AUTO=update`, `SECRET`, `APP_VERSION`, `ADMIN_ROLE_NAME` and `ADMIN_ROLE_DOMAIN`.

Render's free tier blocks outbound SMTP on ports 25, 465 and 587, so Gmail can't send from there. Use an email provider that offers port 2525 or an HTTP API instead.
