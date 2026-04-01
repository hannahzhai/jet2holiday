# Portfolio Management System Frontend (MVP)

Vue 3 + Vite frontend for a single-user portfolio management dashboard.

## Tech Stack
- Vue 3 (Composition API, JavaScript)
- Vite
- Pinia
- Vue Router
- Axios
- ECharts (`vue-echarts`)

## Setup
1. Install dependencies:
   ```bash
   npm install
   ```
2. Configure environment:
   - Copy `.env.example` to `.env`
   - `VITE_API_BASE_URL`
     - leave empty in local dev to use Vite proxy (`/api` -> `http://localhost:8080`) and avoid CORS
     - set full backend URL (for non-proxy deployments), e.g. `http://localhost:8080`
   - `VITE_USE_MOCK_API` (`true` to use in-memory mock backend, default `false`)
3. Run development server:
   ```bash
   npm run dev
   ```
4. Build for production:
   ```bash
   npm run build
   ```

## Mock vs Real Mode
- **Mock mode**: set `VITE_USE_MOCK_API=true`
  - Uses local in-memory data for account, holdings, dashboard summary/performance, and market refresh.
  - Supports add/edit/delete holding, cash update, and chart interactions without backend.
- **Real mode**: set `VITE_USE_MOCK_API=false`
  - Calls Spring Boot REST API.
  - In local dev, proxy mode avoids browser CORS issues.

## Routes
- `/dashboard` (default)
- `/holdings`

## Notes
- No authentication/login flow is included.
- Dashboard summary API (or mock equivalent) is the primary source of calculated portfolio values.