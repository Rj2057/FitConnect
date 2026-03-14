# FitConnect Frontend

This frontend is built with React, Vite, Tailwind CSS, React Router, and Axios.

## Features

- JWT-based login and registration flows
- Role-aware navigation for gym user, trainer, and owner accounts
- Dashboards for gyms, trainers, memberships, bookings, workouts, attendance, streaks, equipment, payments, and owner operations
- Connected to the Spring Boot backend APIs under `/api`

## Development

```bash
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` to the backend.

## Build

```bash
npm run build
```

## Optional Environment Variable

Create a `.env` file if you want to point to a different backend base URL without using the proxy:

- `VITE_API_BASE_URL=http://localhost:8080`
