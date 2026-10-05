# Code-Quality-Analysis

AI Code Review & Governance Platform built with Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA, and PostgreSQL.

## 🚀 Overview

The **AI Code Review & Governance Platform** is an enterprise-grade automated code quality analysis and pull request governance engine. It connects seamlessly to GitHub via GitHub Apps, listens to repository webhooks, and orchestrates asynchronous AI-driven code reviews to detect security vulnerabilities, governance violations, performance bottlenecks, and code smells.

---

## 🛠️ Architecture & Technology Stack

- **Backend:** Java 21, Spring Boot
- **Security:** Spring Security, Stateless JWT (Access & Refresh Tokens), HMAC-SHA256 GitHub Webhook Validation
- **Database:** PostgreSQL with Spring Data JPA & Hibernate DDL management
- **Architecture:** Modular Monolith (Layered: Controller, Service, Repository, Entity, DTO, Mapper, Exception)
- **Async Execution:** Spring `@EnableAsync` with dedicated thread pools for webhook ingestion and AI workers
- **Documentation:** OpenAPI 3 & Swagger UI

---

## 📋 Core Modules

1. **Authentication Module:**
   - User registration & email verification flow
   - Secure login with JWT issuance (Access + Refresh tokens)
   - Role-Based Access Control (`ROLE_DEVELOPER`, `ROLE_TEAM_LEAD`, `ROLE_ADMIN`)
   - GitHub OAuth2 social authentication

2. **Project Module:**
   - Multi-tenant software project creation and lifecycle management
   - Role-based project isolation (Developers manage own, Team Leads view team, Admins manage all)
   - Executive dashboard aggregation (metrics, review counts, vulnerability breakdown)

3. **GitHub App Integration & Webhooks:**
   - Seamless GitHub App installation URL generation with signed state
   - Installation callback handling and repository mapping
   - `POST /api/webhooks/github` endpoint verifying `X-Hub-Signature-256` HMAC signatures
   - Non-blocking asynchronous processing for `push` and `pull_request` (`opened`, `synchronize`, `reopened`) events

4. **AI Review Engine & Findings:**
   - Automated review job creation (`QUEUED` -> `PROCESSING` -> `COMPLETED`)
   - Granular vulnerability detection (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`) across Security, Performance, and Governance
   - Actionable suggested code fixes with file path and line number mapping

---

## 🚦 Getting Started

### Prerequisites
- JDK 21+ (Tested on JDK 24)
- PostgreSQL (Default: port `5434`, database: `ai_code_review`)

### Running Locally
```powershell
# Set JAVA_HOME
$env:JAVA_HOME="C:\Program Files\Java\jdk-24"

# Run tests
.\mvnw.cmd test

# Start the application
.\mvnw.cmd spring-boot:run
```

The server will start on `http://localhost:8080`.
Swagger UI documentation is available at `http://localhost:8080/swagger-ui.html`.
