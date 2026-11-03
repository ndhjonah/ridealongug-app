# RideAlongUG

A car rental platform for Uganda - customers can browse and book cars, trucks and tractors, owners can list their own vehicles, and admins handle verification, payouts and fleet oversight. Built as an internship project.

## Structure

- `backend/` - Spring Boot API (Java, Spring Boot 4.1.0)
- `frontend/` - Angular app (Angular 19)

## Running it locally

You'll need Java 17+, Maven, Node, and a Postgres instance running somewhere.

### 1. Database

Create a Postgres database for the project. Any name works, just make sure it matches what you put in the properties file in the next step.

### 2. Backend

Inside `backend/ridealongug-backend/src/main/resources/`, copy `application.properties.example` to a new file (`application.properties` or `application-dev.properties`, depending on which profile you're running) and fill in your own values - DB URL, username, password, JWT secret, and mail credentials if you're working with the email feature.

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

If your backend isn't running on `localhost:8080`, update the base URL in `frontend/src/app/core/api.service.ts` before starting.

## Branching

Branch off `main`: `feature/<area>-<short-description>`. Open a PR into `main` rather than pushing directly, especially now that there's more than one of us on this.
