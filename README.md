# 🏦 Enterprise Banking Management System

> Production-grade full-stack banking platform built with **Java 21**, **Spring Boot 3**, **Spring Security (JWT)**, **Spring Data JPA**, **PostgreSQL**, and **React**. Engineered for strict ACID guarantees, optimistic locking for concurrency control, permanent financial audit trails, and role-based access.

![Status: Development](https://img.shields.io/badge/Status-DEVELOPMENT-amber.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![React](https://img.shields.io/badge/React-18%2B-cyan.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)

---

## 📌 Project Overview

The **Enterprise Banking Management System** is an end-to-end financial transaction platform built to handle core retail banking workflows. It provides separate portals for **Customers** (account balances, deposits, withdrawals, fund transfers, transaction histories with multi-criteria filtering) and **Administrators** (system-wide oversight, real-time metrics, customer audits, and instant account freezing/unfreezing).

The system enforces strict financial business rules:
- **No negative balances or invalid withdrawals**
- **Strict balance checks before processing fund transfers**
- **Permanent, immutable transaction ledgers**
- **Optimistic concurrency control (`@Version`)** to prevent race conditions during simultaneous deposits/transfers
- **BCrypt password hashing** with stateless JWT authentication and role-based authorization

---

## 🚀 Key Features

### 👤 Customer Capabilities
- **Authentication**: Registration with auto-provisioned checking/savings accounts, secure JWT login, and session persistence.
- **Account Dashboard**: Live balance tracking, account details, and real-time status monitoring (ACTIVE / FROZEN).
- **Deposit Operations**: Funds deposit with instant balance updates and audit trail reference generation.
- **Withdrawal Operations**: Balance checks ensuring overdraft protection; blocked automatically if the account is frozen.
- **Inter-Account Transfers**: ACID-compliant fund transfers between accounts with source/target account validation.
- **Transaction History**: Filter by transaction type (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`), date ranges, and reference number search.

### 🛡️ Administrator Operations
- **Executive Metrics**: High-level financial KPIs including Total Deposited Volume, Total Active Accounts, Customer Count, and System Volume.
- **Account Oversight & Freezing**: Real-time control to freeze suspicious accounts or unfreeze cleared accounts, immediately preventing or re-enabling transactional capabilities.
- **Customer Directory**: Paginated search across customer names, emails, and linked bank accounts.
- **Global Transaction Ledger**: Complete chronological audit log of all system transactions.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Spring Security, Hibernate |
| **Security** | JSON Web Tokens (JJWT 0.12.5), BCrypt Password Hashing, Role-Based Access Control (RBAC) |
| **Database** | PostgreSQL 16 (Primary Production), H2 Database (In-Memory Development/Test Fallback) |
| **Build & Tooling** | Apache Maven 3.9+, Spring Boot Maven Plugin, Lombok |
| **API Docs** | OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui`) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **Frontend** | React 18, Vite, Vanilla CSS (Modern Fintech Glassmorphism), Lucide Icons |
| **DevOps** | Multi-Stage Dockerfile, Docker Compose |

---

## 🏛️ Architecture & Clean Design Pattern

The backend follows the industry-standard clean layered architecture:

```
com.portfolio.banking/
├── config/                 # Security, CORS, Swagger, Database seed runner
├── controller/             # RESTful API controllers with HTTP status mapping
├── dto/
│   ├── request/            # Validated request payloads (@Valid, @NotNull, @DecimalMin)
│   └── response/           # Structured response payloads
├── entity/                 # JPA Entities (User, Account, Transaction, Role)
├── exception/              # GlobalExceptionHandler (@RestControllerAdvice) & domain exceptions
├── mapper/                 # Entity <-> DTO translation components
├── repository/             # Spring Data JPA repositories with custom queries
├── security/               # Custom UserDetails, JWT Filter, Token Provider
├── service/                # Business logic contracts
│   └── impl/               # Transactional implementations (@Transactional)
└── util/                   # Account number generators & helpers
```

### 🔒 Concurrency & ACID Guarantees
- Financial operations execute inside `@Transactional(isolation = Isolation.READ_COMMITTED)`.
- The `Account` entity leverages `@Version private Long version;`. If two simultaneous requests attempt to debit or credit the same account concurrently, JPA detects the version conflict, aborts the second transaction, and throws an `OptimisticLockingFailureException`, which our `GlobalExceptionHandler` converts into a clean `409 CONFLICT` response rather than corrupting balances.

---

## 🗄️ Database Design

```
+--------------------+            +------------------------+
|       users        | 1        * |        accounts        |
+--------------------+------------+------------------------+
| id (PK)            |            | id (PK)                |
| full_name          |            | account_number (UNIQUE)|
| email (UNIQUE)     |            | balance (DECIMAL 19,4) |
| password (HASHED)  |            | account_type           |
| role_id (FK)       |            | status (ACTIVE/FROZEN) |
| created_at         |            | version (LOCKING)      |
+--------------------+            | user_id (FK)           |
                                  +------------------------+
                                              | 1
                                              |
                                              | *
                                  +------------------------+
                                  |      transactions      |
                                  +------------------------+
                                  | id (PK)                |
                                  | reference_number (UNQ) |
                                  | from_account_id (FK)   |
                                  | to_account_id (FK)     |
                                  | transaction_type       |
                                  | amount (DECIMAL 19,4)  |
                                  | balance_after          |
                                  | status                 |
                                  | timestamp              |
                                  +------------------------+
```

---

## 📡 REST API Documentation

### Authentication (`/api/v1/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Register customer & provision initial account | None |
| `POST` | `/api/v1/auth/login` | Login and obtain JWT token | None |

### Customer Endpoints (`/api/v1/customer`)
| Method | Endpoint | Description | Role |
|---|---|---|---|
| `GET` | `/api/v1/customer/profile` | View profile and accounts | `CUSTOMER`, `ADMIN` |
| `GET` | `/api/v1/customer/accounts` | List customer bank accounts | `CUSTOMER`, `ADMIN` |
| `GET` | `/api/v1/customer/accounts/{acc}/balance` | Live balance inquiry | `CUSTOMER`, `ADMIN` |
| `POST` | `/api/v1/customer/accounts` | Open additional savings/checking account | `CUSTOMER` |
| `POST` | `/api/v1/customer/transactions/deposit` | Deposit funds | `CUSTOMER` |
| `POST` | `/api/v1/customer/transactions/withdraw` | Withdraw funds | `CUSTOMER` |
| `POST` | `/api/v1/customer/transactions/transfer` | Transfer funds | `CUSTOMER` |
| `GET` | `/api/v1/customer/accounts/{acc}/transactions` | Paginated & filtered transaction history | `CUSTOMER` |

### Admin Endpoints (`/api/v1/admin`)
| Method | Endpoint | Description | Role |
|---|---|---|---|
| `GET` | `/api/v1/admin/dashboard` | Platform metrics & totals | `ADMIN` |
| `GET` | `/api/v1/admin/customers` | Search customers with pagination | `ADMIN` |
| `GET` | `/api/v1/admin/accounts` | View all system accounts | `ADMIN` |
| `PUT` | `/api/v1/admin/accounts/{acc}/freeze` | Freeze an account | `ADMIN` |
| `PUT` | `/api/v1/admin/accounts/{acc}/unfreeze` | Unfreeze an account | `ADMIN` |
| `GET` | `/api/v1/admin/transactions` | Full system audit trail | `ADMIN` |

---

## 📖 Swagger / OpenAPI Specification

Once running locally, interactive OpenAPI documentation with "Try it out" capabilities is accessible at:
```
http://localhost:8080/swagger-ui.html
```
Raw OpenAPI 3 JSON schema:
```
http://localhost:8080/v3/api-docs
```

---

## ⚙️ Environment Variables

Create an `.env` file or export the following variables:

| Variable | Description | Default |
|---|---|---|
| `PORT` | Backend server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev`, `prod`, `test`) | `dev` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC Connection URL | `jdbc:postgresql://localhost:5432/banking_db` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL User | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL Password | `postgres` |
| `JWT_SECRET` | 256-bit Base64 secret key | Pre-configured safe default |
| `JWT_EXPIRATION_MS` | Token validity in milliseconds | `86400000` (24 Hours) |

---

## 💻 Local Setup & Execution

### Prerequisites
- **Java 21 JDK**
- **Maven 3.9+**
- **Node.js 18+ & npm**
- **PostgreSQL 16** (or use Docker Compose)

### 1. Backend Setup
```bash
cd backend

# Run automated tests
mvn test

# Start the Spring Boot application (defaults to dev profile with automatic in-memory DB or PostgreSQL)
mvn spring-boot:run
```

The application pre-populates demo accounts on first launch:
- **Admin**: `admin@bank.com` / `AdminPassword@123`
- **Customer 1**: `customer@bank.com` / `CustomerPassword@123` (Acc: `101000000001`, Balance: `$15,000.00`)
- **Customer 2**: `alice@bank.com` / `CustomerPassword@123` (Acc: `101000000002`, Balance: `$8,500.00`)

### 2. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start Vite dev server
npm run dev
```
Open your browser at `http://localhost:5173`.

---

## 🐳 Docker Deployment

To spin up the entire production environment with PostgreSQL and Spring Boot:

```bash
# From the project root
docker-compose up --build
```
This runs PostgreSQL on port `5432` and the Spring Boot backend on port `8080`.

---

## 🧪 Testing

Execute unit and integration tests verifying deposit logic, withdrawal protection, transfer concurrency, and account freezing:

```bash
cd backend
mvn test
```

Test suite coverage highlights:
- `testDeposit_Success`: Balance increase and permanent transaction creation
- `testDeposit_InvalidAmount`: Rejection of <= $0 amounts
- `testWithdrawal_Success`: Debit logic and post-transaction balance calculation
- `testWithdrawal_InsufficientBalance`: Prevention of overdraft and account protection
- `testWithdrawal_FrozenAccount`: Immediate rejection when account status is `FROZEN`
- `testTransfer_Success`: Atomic sender debit, recipient credit, and reference creation
- `testTransfer_InsufficientBalance`: Safe rollback on insufficient sender funds
- `testTransfer_SameAccount_Fails`: Disallows self-transfer loops

---

## 🔮 Future Improvements
- Scheduled recurring transfers and standing orders
- Two-Factor Authentication (2FA via TOTP / SMS OTP)
- Statement generation in PDF format with digital signatures
- Webhook notifications for credit/debit alerts

---

## 👨‍💻 Author
**Praveenkumar T**  
- Portfolio: Coming Soon
- GitHub: [githubUrl: ""](Coming Soon)
- Live Demo: [liveUrl: ""](Coming Soon)
