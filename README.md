![CI](https://github.com//Femcoders-SleepUp/SleepUp/actions/workflows/ci.yml/badge.svg)
# 💤 SleepUp — Backend API

**SleepUp** is a backend application designed to manage accommodations and reservations in a secure, scalable environment.  
It is built with **Java 21** and **Spring Boot 3.5.5**, featuring **JWT-based authentication**, **Redis token blacklisting**, **Cloudinary image storage**, **email notifications**, and robust CI/CD pipelines.  
The API includes stateless session handling, Swagger-based documentation, and clear domain separation for maintainability.

---

## 📋 Index
- [🗝 Key Features](#key-features-)
    - [Security and Authentication 🔒](#security-and-authentication-)
    - [User Management 👥](#user-management-)
    - [Accommodation Management 🏨](#accommodation-management-)
    - [Reservation Management 📅](#reservation-management-)
    - [API Documentation 📜](#api-documentation-)
    - [Exception Handling ⚠️](#exception-handling-)
- [👩‍💻 Technology Stack](#technology-stack-)
- [📁 Clone the Repository](#clone-the-repository-)
    - [Run 🏃‍♂️](#run-)
- [📚 API Endpoints](#-api-endpoints)
    - [Authentication](#authentication)
    - [Users](#users)
    - [Accommodations](#accommodations)
    - [Reservations](#reservations)
- [🧪 Running Tests](#running-tests-)
- [🚀 CI/CD Pipeline](#cicd-pipeline)
- [📦 Diagrams](#diagrams-)
- [👥 Contributors](#contributors-)

---

## 🗝 Key Features

### Security and Authentication 🔒
- Stateless authentication using **JWT + Refresh Tokens**
- Role-based access control: `USER` and `ADMIN`
- Route protection with JWT Bearer tokens
- **Token blacklisting** using Redis
- Email notifications:
    - Confirmation upon user registration
    - Notification on specific actions (e.g., reservation reminders)

---

### User Management 👥
- Register and log in users
- Access and update personal profiles
- Admin-only features:
    - View all registered users
    - Update user details and roles
    - Delete users

---

### Accommodation Management 🏨
- Full **CRUD** for accommodations
- Users manage their own listings
- Admins can manage all accommodations
- Store images using **Cloudinary**
- Filter by **name**, **location**, **price range**, **guest capacity**, and **pet-friendly**
- Pagination and sorting supported

---

### Reservation Management 📅
- Users can create, update, and cancel reservations
- Owners can confirm or reject reservations
- Filter reservations by **time period**: ALL, PAST, FUTURE
- Pagination and status tracking

---

### API Documentation 📜
- Integrated with **Swagger UI** for easy API exploration and testing.

---

### Exception Handling ⚠️
- Global exception handling using `@ControllerAdvice` (`GlobalExceptionHandler`)
- Clear and structured error messages
- Covers validation, authorization, and domain-specific exceptions

---

## 👩‍💻 Technology Stack

| Component        | Technology         | Version   | Purpose                             |
|------------------|--------------------|-----------|-------------------------------------|
| Framework        | Spring Boot        | 3.5.5     | Main application framework          |
| Runtime          | Java               | 21        | Runtime environment                 |
| Database         | MySQL              | Latest    | Persistent data storage             |
| Caching          | Redis              | Latest    | Token blacklist                     |
| Security         | Spring Security    | 6.x       | Authentication & authorization      |
| JWT              | JJWT               | 0.12.6    | Token generation & validation       |
| Email            | Spring Mail        | 3.5.5     | Email notifications                 |
| Images           | Cloudinary         | 2.0.0     | Image storage & management          |
| Documentation    | SpringDoc OpenAPI  | 2.8.9     | API documentation                   |
| Mapping          | MapStruct          | 1.5.5     | Entity-DTO transformations          |
| Testing          | Spring Boot Test   | 3.5.5     | Unit & integration testing          |

---

## 📁 Clone the Repository

```bash
git clone https://github.com/Femcoders-SleepUp/SleepUp.git
cd SleepUp
```

### Run 🏃‍♂️

```bash
./mvnw spring-boot:run
```
or
```bash
mvn spring-boot:run
```

> 💡 Alternatively, run the main application class (annotated with `@SpringBootApplication`) directly from your IDE, e.g., IntelliJ IDEA → right-click → **Run 'SuApplication.main()'**.

---

## 📚 API Endpoints

### Authentication
- `POST /auth/register` — Register a new user
- `POST /auth/login` — Log in and receive JWT token
- `POST /auth/logout` — Invalidate current token
- `POST /auth/refresh` — Refresh expired token

---

### Users
- `GET /users/profile` — Get current user profile
- `PUT /users/profile` — Update current user profile
- `DELETE /users/profile` — Delete current user account
- `POST /users/upload-avatar` — Upload avatar image
- `GET /admin/users` — List all users (ADMIN)
- `GET /admin/users/{id}` — Get user by ID (ADMIN)
- `PUT /admin/users/{id}` — Update user by ID (ADMIN)
- `DELETE /admin/users/{id}` — Delete user by ID (ADMIN)

---

### Accommodations
- `GET /accommodations` — List all accommodations
- `GET /accommodations/{id}` — Get details by ID
- `GET /accommodations/filter` — Filter accommodations with pagination
- `POST /accommodations` — Create a new accommodation (USER)
- `PUT /accommodations/{id}` — Update accommodation (OWNER)
- `DELETE /accommodations/{id}` — Delete accommodation (OWNER)

---

### Reservations
- `GET /reservations` — List user’s reservations
- `GET /reservations/{id}` — Get reservation details
- `POST /reservations` — Create new reservation
- `PUT /reservations/{id}` — Update reservation
- `DELETE /reservations/{id}` — Cancel reservation
- `GET /owners/reservations` — List reservations for owner’s accommodations
- `PUT /owners/reservations/{id}/confirm` — Confirm reservation
- `PUT /owners/reservations/{id}/cancel` — Cancel reservation

---

## 🧪 Running Tests
- Tests run in a dedicated **MySQL test database**
- Configured with SQL scripts (`schema.sql` and `data.sql`) for isolation
- External services (Cloudinary, email) mocked for reliability

```bash
./mvnw test
```

---

## 🚀 CI/CD Pipeline
- **GitHub Actions** with automated workflows:
    - `test.yml` — PR-triggered testing using Docker
    - `ci.yml` — Continuous integration for main/dev branches
    - `build.yml` — Build and publish Docker image
    - `release.yml` — Tag-based release pipeline with full test suite

---

## 📦 Diagrams
- **EER Diagram** — Entity-relationship overview of database schema
- **Class Diagram** — Core domain classes and relationships
- **Flow Diagrams** — Illustrating background tasks and workflows

---

## 👥 Contributors
- **Paula Calvo** — [GitHub](https://github.com/PCalvoGarcia)
- **Iris Sánchez** — [GitHub](https://github.com/isanort/)
- **Bruna Sonda** — [GitHub](https://github.com/brunasonda)
- **Paola Pinilla** — [GitHub](https://github.com/PaolaAPL17)
- **Thais Rocha** — [GitHub](https://github.com/thaisrqueiroz)

