# 🛡️ Token Management Service 🛡️

A minimal REST API for securely managing user tokens.

---

## 📌 Features

- Generate API tokens (and store hashed versions)
- Rotate tokens
- Invalidate tokens
- List all tokens per user with metadata
- Audit log of all operations
- Timestamps: created, rotated, last used
- Secure: raw tokens are never stored
- Input validation on all endpoints

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot 3
- PostgreSQL
- Spring Data JPA
- Hibernate
- Docker Compose (PostgreSQL)
- SLF4J + Logback (logging)
- Jakarta Validation

---

## 🚀 Setup Instructions

### 📦 1. Clone the Repository

```bash
git clone https://github.com/orenIsabella/token-management-service.git
cd token-management-service
```

### 🐳 2. Start PostgreSQL via Docker

```bash
docker compose up -d
```

- Database: `tokendb`
- Username: `postgres`
- Password: `postgres`
- Port: `5432`

### 🏃 3. Run the App

```bash
mvn spring-boot:run
```

App will be available at [http://localhost:8080](http://localhost:8080)

---

## 🔍 API Endpoints

### 🔐 Generate Token

```http
POST /tokens?userId=bob
```

**Returns**:
```json
{
  "token": "raw-token-value-here",
  "userId": "bob"
}
```

---

### 🔁 Rotate Token

```http
POST /tokens/rotate?oldToken=raw-token-value
```

**Returns**:
```json
{
  "token": "new-raw-token-value",
  "userId": "bob"
}
```

---

### ❌ Invalidate Token

```http
DELETE /tokens/{tokenValue}
```

---

### 📋 List Tokens

```http
GET /tokens?userId=bob
```

Returns all hashed tokens with metadata (creation, last used, rotated).

---

### 📜 List All Tokens for a User
**GET /tokens**

Query Param:
- `userId` (required)

Returns all tokens (valid and invalid) for the user.

---

### 🧾 Get Audit Logs

```http
GET /auditlogs?userId=bob
```

Returns all actions (generate, rotate, invalidate) for the user.

---

## 📄 Assumptions & Notes

- Tokens are returned only once at creation/rotation
- Tokens are hashed before storing in the database due to secirity reasons
- `userId` is a simple string (no authentication layer)
- Input validation is enforced using `@NotBlank`
- Database connection is configured for Docker, DB is named `tokendb`, username/password is `postgres`
- Token hashing (SHA-256)
- Structured logging to `logs/app.log`

---

## 📁 Log File

Logs are written to:
```
logs/app.log
```

---

## 🧪 Tests

Manual tests can be run using **Insomnia**, **Postman**, or `curl`. (Automated tests can be added later.)
