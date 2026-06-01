# Job Hunt Tracker — CLAUDE.md

## Who I am
Anuththara Kavindi — Software Engineer with 4+ years backend experience
(Java/Spring Boot at Kaleris, Silentium; Node.js at GDC NZ).
Currently job-hunting in New Zealand, targeting mid-level backend roles.
Master of Applied IT (Distinction) from Wintec NZ.

## Project goal
Build a portfolio-grade full-stack app demonstrating production thinking
and AI integration. Phase 1 priorities:
1. JWT authentication
2. Global exception handling and validation
3. AI cover letter generator with Anthropic Claude API
4. Deployed live on Railway (backend) and Vercel (frontend)
5. README that impresses NZ hiring managers

## Tech stack
- Java 17, Spring Boot 3.5.x
- Spring Data JPA + MySQL 8
- Spring Security with JWT
- Bean Validation
- Lombok
- springdoc-openapi (Swagger UI)
- WebClient for Anthropic Claude API calls
- JUnit 5 + Mockito for tests

## Conventions to enforce
- Layered architecture: controller → service → repository → entity
- DTOs for all request/response bodies (never expose entities directly)
- Constructor injection only — no @Autowired field injection
- Global @RestControllerAdvice for exception handling
- Conventional Commits (feat, fix, refactor, chore, test, docs)
- No secrets in code — application.properties placeholders + env vars only

## How to work with me
- Explore → Plan → Code → Commit workflow on every feature
- Before writing code, summarise the plan and wait for me to approve
- After writing code, explain every annotation and pattern used
- Point out edge cases and what a senior reviewer would critique
- Ask me to explain back any concept that's new
- Suggest meaningful commit messages — I'll review before committing

## What NOT to do
- No @Autowired field injection
- Don't expose JPA entities from controllers
- Don't add Docker, MapStruct, or API versioning yet (later phases)
- Don't add features I didn't ask for
- Don't write code without explaining the concept first