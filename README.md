# FitConnect

FitConnect is a full stack gym platform with three roles:

1. Gym User
2. Gym Trainer
3. Gym Owner

It includes authentication, gym discovery, memberships, trainer bookings, trainer ratings, workouts, attendance, equipment, and payments.

## Tech Stack

1. Backend: Java 24, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, JWT
2. Frontend: React, Vite, Tailwind CSS, Axios, React Router
3. API Docs: Swagger (SpringDoc)

## Quick Start For Friends

1. Install prerequisites listed in [requirements.txt](requirements.txt)
2. Create PostgreSQL database named fitconnect
3. Set backend environment variables (or update properties)
4. Start backend from project root: mvn spring-boot:run
5. Start frontend from [frontend](frontend):
	1. npm install
	2. npm run dev

## Environment Variables

Backend variables:

1. DB_URL (example: jdbc:postgresql://localhost:5432/fitconnect)
2. DB_USERNAME
3. DB_PASSWORD
4. APP_JWT_SECRET
5. APP_JWT_EXPIRATION_MS
6. APP_SEED_ENABLED
7. APP_CORS_ALLOWED_ORIGINS

Frontend variables:

1. VITE_API_BASE_URL
2. VITE_BACKEND_ORIGIN

You can check sample frontend env values in [frontend/.env.example](frontend/.env.example).

## Default URLs

1. Frontend app: http://localhost:5173
2. Backend API: http://localhost:8080
3. Swagger UI: http://localhost:8080/swagger-ui.html
4. OpenAPI JSON: http://localhost:8080/v3/api-docs

## Seed Accounts

When APP_SEED_ENABLED is true and database is empty:

1. owner@fitconnect.com / Owner@123
2. trainer@fitconnect.com / Trainer@123
3. user@fitconnect.com / User@123

## Main Functional Modules

1. Auth with JWT
2. Gym creation, fee management, details, reviews
3. Trainer profile and user driven trainer ratings
4. Membership plans and auto pricing
5. Trainer bookings with slot rules
6. Attendance with auto gym from active membership
7. Workout history and weekly calories
8. Equipment management
9. Payments
10. Streak tracking

## Useful Commands

From project root:

1. Run backend: mvn spring-boot:run
2. Run backend tests: mvn test

From [frontend](frontend):

1. Install deps: npm install
2. Run dev server: npm run dev
3. Build: npm run build

## GitHub Upload Steps

From project root:

1. git init
2. git add .
3. git commit -m "Initial FitConnect full stack project"
4. Create a new empty GitHub repo
5. git branch -M main
6. git remote add origin YOUR_GITHUB_REPO_URL
7. git push -u origin main

## Notes For Evaluators

Architecture and OOAD alignment are implemented through:

1. Layered MVC structure (controller, service, repository, entity, dto)
2. Role based authorization and clear separation of concerns
3. Practical use of abstraction, encapsulation, composition, and design principles
