# Job Hunt Tracker API — Project Overview

## Project Structure

The codebase is organized into 7 feature packages plus a `security` and `common` layer.

| Package | Classes | What it does |
|---|---|---|
| `auth` | `AuthController`, `AuthService`, `EmailAlreadyExistsException` + 3 DTOs | Register/login endpoints, JWT generation on auth |
| `company` | `CompanyController`, `CompanyService`, `CompanyRepository`, `Company` entity, `CompanyNotFoundException` + 2 DTOs | CRUD for companies, scoped to the authenticated user |
| `jobapplication` | `JobApplicationController`, `JobApplicationService`, `JobApplicationRepository`, `JobApplication` entity, `JobApplicationNotFoundException`, `JobApplicationStatus` enum, `WorkType` enum + 2 DTOs | CRUD for job applications, also scoped to the authenticated user |
| `coverletter` | `CoverLetterController`, `CoverLetterService`, `CoverLetterGenerationException` + 2 DTOs | Generates a cover letter by building a prompt and calling Anthropic |
| `coverletter.anthropic` | `AnthropicClient` + 2 DTOs (`AnthropicRequest`, `AnthropicResponse`) | WebClient wrapper for the Anthropic Claude API — handles HTTP, error mapping |
| `security` | `JwtService`, `JwtAuthenticationFilter`, `JwtAuthEntryPoint`, `SecurityConfig`, `SecurityUtils`, `UserDetailsServiceImpl` | JWT token lifecycle, filter chain, loading `UserDetails` from DB |
| `common.exception` | `GlobalExceptionHandler`, `ErrorResponse` | Converts every exception to a structured JSON error response |
| `common.health` | `HealthController` | Simple `/api/health` ping endpoint |
| `user` | `User`, `UserRepository`, `Role` | JPA entity and repository; `User` implements `UserDetails` |

---

## Existing Test Coverage

| Test file | Type | What's covered |
|---|---|---|
| `AuthServiceTest` | Unit (Mockito) | register success, duplicate email, login success, wrong password |
| `AuthControllerIntegrationTest` | Integration (MockMvc + H2) | register 201, register 400 validation, register 409 duplicate, login 200, login 401 |
| `CompanyServiceTest` | Unit (Mockito) | create, getById, getById not found, update, delete |
| `CompanyControllerIntegrationTest` | Integration | create without token → 401, create with token → 201, cross-user access → 404 |
| `JobApplicationServiceTest` | Unit (Mockito) | create, getById not found, update |

---

## Classes With Zero Test Coverage

### Security layer — entirely untested
- `JwtService` — real logic (token generation, email extraction, expiry check)
- `JwtAuthenticationFilter` — `Bearer ` header parsing and SecurityContext setup
- `JwtAuthEntryPoint` — sends the 401 JSON response for unauthenticated requests
- `SecurityUtils` — extracts the current `User` from `SecurityContextHolder`
- `UserDetailsServiceImpl` — loads `User` by email from `UserRepository`

### Cover letter feature — entirely untested
- `CoverLetterService` — prompt builder + owner-scoped not-found check
- `AnthropicClient` — WebClient call, null response guard, error mapping to `CoverLetterGenerationException`
- `CoverLetterController` — endpoint has no integration test

### Other gaps
- `JobApplicationController` — service is tested but no HTTP-layer test exists (list, getById, delete, status filtering all uncovered)
- `GlobalExceptionHandler` — exercised indirectly by integration tests but the `CoverLetterGenerationException → 502` and `AccessDeniedException → 403` paths are never hit
- `HealthController` — trivial but completely uncovered

---

## Testing Dependencies

### Already in `pom.xml` (via `spring-boot-starter-test`)
- **JUnit 5 (Jupiter)** — test runner
- **Mockito + `MockitoExtension`** — unit test mocking
- **AssertJ** — fluent assertions
- **Spring `MockMvc`** — HTTP layer testing without a live server
- **`@SpringBootTest` + H2** — full integration tests against an in-memory DB

### Missing
| Dependency | Why you need it |
|---|---|
| `spring-security-test` (`org.springframework.security:spring-security-test`) | Needed for `@WithMockUser` and `SecurityMockMvcRequestPostProcessors.jwt()`. Without it, controller tests can't set up a mock JWT principal, so you're forced into heavyweight `@SpringBootTest` for every controller test. |
| `reactor-test` (`io.projectreactor:reactor-test`) | Useful for testing reactive `WebClient` flows in `AnthropicClient`. Not strictly required since `.block()` is used, but good practice when working with WebFlux. |

---

## Priority Order for New Tests

1. **`JwtServiceTest`** — pure unit test, no Spring context needed, tests the core security primitive
2. **`UserDetailsServiceImplTest`** — unit test, straightforward mock of `UserRepository`
3. **`CoverLetterServiceTest`** — unit test, mock `AnthropicClient` and `JobApplicationRepository`
4. **`AnthropicClientTest`** — unit test, mock `WebClient` to verify error handling paths
5. **`JobApplicationControllerIntegrationTest`** — integration test, covers the full HTTP layer gap
6. **`CoverLetterControllerIntegrationTest`** — integration test, mock `AnthropicClient` bean to avoid real API calls
7. **`GlobalExceptionHandlerTest`** — unit test with `@WebMvcTest`, covers the untested 502 and 403 handlers

> Before writing controller slice tests (`@WebMvcTest`), add `spring-security-test` to `pom.xml` — it unlocks `@WithMockUser` and avoids full context startup for controller-layer tests.