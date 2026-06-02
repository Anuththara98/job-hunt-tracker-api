
# Job Hunt Tracker API

A production-grade REST API for tracking job applications, built with Java 17 and Spring Boot 3.5. Includes JWT authentication, per-user data scoping, Bean Validation, structured error handling, and an AI-powered cover letter generator using the Anthropic Claude API.

---

## Features

- **JWT Authentication** — stateless Bearer token auth; register and login endpoints; BCrypt password hashing
- **User-scoped data** — every company and job application is private to the authenticated user; no cross-user data leakage
- **AI Cover Letter Generator** — generates tailored cover letters via the Anthropic Claude API based on job title, company, work type, and applicant background
- **Global Exception Handling** — consistent JSON error responses across all error types (400, 401, 403, 404, 409, 502)
- **Bean Validation** — request-level validation with field-level error messages
- **Audit Timestamps** — `createdAt` / `updatedAt` on all entities via Hibernate annotations
- **Swagger UI** — interactive API docs available at `/swagger-ui.html`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 + JJWT 0.12 |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| HTTP Client | Spring WebClient (WebFlux) |
| AI | Anthropic Claude API (`claude-haiku-4-5`) |
| Validation | Jakarta Bean Validation |
| Docs | springdoc-openapi (Swagger UI) |
| Build | Maven |

---

## Architecture

Strict layered architecture — no layer skips, no entity exposure through controllers.

```
Controller  →  Service  →  Repository  →  Entity
               ↓
           AnthropicClient (external API)
```

- **DTOs** for all request and response bodies — JPA entities never leave the service layer
- **Constructor injection** throughout — no `@Autowired` field injection
- **`@Transactional`** on all service write methods; `readOnly = true` on reads
- **`SecurityUtils.getCurrentUser()`** resolves the authenticated user from `SecurityContextHolder` in any service method
- **Owner-scoped repository queries** — `findByIdAndOwner(id, user)` ensures users can only access their own data; returns 404 (not 403) to avoid resource enumeration

---

## API Endpoints

All endpoints except `/api/auth/**` and `/api/health` require:
```
Authorization: Bearer <token>
```

### Auth
| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Create account, returns JWT | No |
| POST | `/api/auth/login` | Login, returns JWT | No |

### Companies
| Method | Path | Description |
|---|---|---|
| POST | `/api/companies` | Create a company |
| GET | `/api/companies` | List your companies |
| GET | `/api/companies/{id}` | Get a company |
| PUT | `/api/companies/{id}` | Update a company |
| DELETE | `/api/companies/{id}` | Delete a company |

### Job Applications
| Method | Path | Description |
|---|---|---|
| POST | `/api/job-applications` | Create a job application |
| GET | `/api/job-applications` | List your applications |
| GET | `/api/job-applications/{id}` | Get an application |
| GET | `/api/job-applications/company/{companyId}` | List by company |
| PUT | `/api/job-applications/{id}` | Update an application |
| DELETE | `/api/job-applications/{id}` | Delete an application |

### AI Cover Letter
| Method | Path | Description |
|---|---|---|
| POST | `/api/job-applications/{id}/cover-letter` | Generate a cover letter using Claude AI |

**Request body:**
```json
{
  "userBio": "Optional — brief background about yourself (max 500 chars)"
}
```

**Response:**
```json
{
  "generatedLetter": "Dear Hiring Manager...",
  "jobTitle": "Backend Software Engineer",
  "companyName": "Xero"
}
```

### Health
| Method | Path |
|---|---|
| GET | `/api/health` |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8

### 1. Clone the repo

```bash
git clone https://github.com/Anuththara98/job-hunt-tracker-api.git
cd job-hunt-tracker-api
```

### 2. Create the database

```sql
CREATE DATABASE job_hunt_tracker_db;
```

### 3. Configure environment variables

The app reads secrets from environment variables — nothing is hardcoded.

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | Base64-encoded secret, min 32 bytes |
| `ANTHROPIC_API_KEY` | Yes | Anthropic API key (`sk-ant-...`) |
| `JWT_EXPIRATION_MS` | No | Token lifetime in ms (default: 86400000 = 24h) |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated origins (default: `http://localhost:3000`) |
| `ANTHROPIC_MODEL` | No | Claude model to use (default: `claude-haiku-4-5-20251001`) |
| `ANTHROPIC_MAX_TOKENS` | No | Max tokens for cover letter (default: 1024) |

Generate a JWT secret:
```bash
openssl rand -base64 32
```

### 4. Update database credentials

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
```

### 5. Run

```bash
export JWT_SECRET=<your-base64-secret>
export ANTHROPIC_API_KEY=<your-anthropic-key>
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.
Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

---

## Example Requests

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","password":"password123"}'
```

### Create a job application
```bash
curl -X POST http://localhost:8080/api/job-applications \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "jobTitle": "Backend Software Engineer",
    "location": "Auckland, NZ",
    "workType": "HYBRID",
    "status": "APPLIED",
    "appliedDate": "2026-06-01",
    "notes": "Java/Spring Boot role, cloud-native team"
  }'
```

### Generate a cover letter
```bash
curl -X POST http://localhost:8080/api/job-applications/1/cover-letter \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userBio": "4 years Java/Spring Boot experience, Master of Applied IT from Wintec NZ"}'
```

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-06-01T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/job-applications",
  "validationErrors": {
    "jobTitle": "Job title is required",
    "status": "Status is required"
  }
}
```

---

## Enums

**`JobApplicationStatus`**: `APPLIED` `INTERVIEW` `OFFER` `REJECTED`

**`WorkType`**: `ONSITE` `REMOTE` `HYBRID`

---

## Author

Anuththara Kavindi — [github.com/Anuththara98](https://github.com/Anuththara98)