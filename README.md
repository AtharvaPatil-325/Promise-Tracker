# PromiseTracker — Production-Grade SaaS

PromiseTracker is a micro-SaaS designed to ensure customer commitments, deadlines, and follow-ups made by sales, support, and account managers are **never forgotten**.

---

## 🌟 Key Features

- **Micro-SaaS Commitment Focus**: Tracks promises, deadlines, customer ownership, and accountability.
- **Multi-Tenant Architecture**: Strict organization tenant isolation enforced at the database query and service layer.
- **Role-Based Access Control (RBAC)**: Supports `OWNER`, `ADMIN`, and `MEMBER` roles with method-level Spring Security checks.
- **Manifest V3 Chrome Extension**: Context menu text selection to create promises directly from emails, web chats, or document portals.
- **Audit Trail & Activity Log**: Records `CREATED`, `UPDATED`, `ASSIGNED`, `STATUS_CHANGED`, `COMPLETED`, and `CANCELLED` actions for full operational accountability.
- **Modern Dashboard & Analytics**: Features real-time counters (Today, Overdue, Completed, Completion Rate) and assignee performance breakdowns.

---

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate, JWT, Flyway, PostgreSQL, Lombok, OpenAPI 3.
- **Frontend**: React 18, TypeScript, Vite, React Router 6, TanStack Query v5, Axios, Tailwind CSS, Lucide Icons.
- **Chrome Extension**: Manifest V3, TypeScript, Vite, Chrome Storage API, Context Menus API.
- **Infrastructure**: Docker, Docker Compose, PostgreSQL 16.

---

## 📁 Repository Structure

```text
promisetracker/
├── backend/            # Spring Boot 3 REST API & Security
├── frontend/           # React + Vite + TypeScript Dashboard
├── extension/          # Manifest V3 Chrome Extension
├── docs/               # Architecture & Threat Model Documentation
│   └── security-threat-model.md
├── docker-compose.yml  # Container orchestration setup
├── .gitignore
└── README.md
```

---

## 🚀 Quick Start (Local Development)

### Prerequisites

- Java 21+
- Node.js 20+
- PostgreSQL 16 (or use embedded H2 for dev)
- Docker & Docker Compose (optional for containerized deployment)

### 1. Database Setup

For local development, the backend uses an embedded H2 database automatically. No PostgreSQL setup required.

### 2. Run Backend API

```bash
cd backend
mvn spring-boot:run
```

The Spring Boot backend will run at `http://localhost:8080` and run Flyway database migrations automatically.

### 3. Run Frontend Dashboard

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000` in your browser.

### 4. Build Chrome Extension

```bash
cd extension
npm install
npm run build
```

Load the unpacked extension in Chrome:
1. Open `chrome://extensions/`
2. Enable **Developer mode** (top right)
3. Click **Load unpacked**
4. Select the `extension/dist` folder

---

## 🔒 Security Architecture

Please see [`docs/security-threat-model.md`](docs/security-threat-model.md) for full threat mitigations. Key principles:
1. **Never Trust Client Input**: All inputs validated with Bean Validation (`@Valid`) and sanitized.
2. **Tenant Isolation (BOLA/IDOR Prevention)**: Every query checks `organization_id`.
3. **Strict Secret Management**: Passwords hashed with BCrypt (`strength = 12`); JWT signed with externalized secrets (`JWT_SECRET`).
4. **Clean DTO Boundaries**: JPA Entities are never directly returned over REST endpoints.

---

## 📜 License

MIT License. See [LICENSE](LICENSE) for details.