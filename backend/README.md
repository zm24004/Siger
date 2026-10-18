# SIGER Backend (Spring Boot 3.5 + Java 21)

REST API for the **Sistema de Gestión de Eventos y Reservas (SIGER)**.

## Tech stack

- Spring Boot 3.5.0, Java 21
- Spring Security (stateless) + JWT (jjwt 0.12.6)
- Spring Data JPA + MySQL 8 (validate mode)
- Bean Validation, Springdoc OpenAPI 2.6.0, Lombok

## Requirements

- JDK 21
- Maven 3.9+
- MySQL 8.0.16+ with the schema loaded from `../database/siger.sql`

## Setup

1. Load the database schema:
   ```bash
   mysql -u root -p < ../database/siger.sql
   ```
2. Create the application DB user (recommended, do **not** use root):
   ```sql
   CREATE USER 'siger_app'@'localhost' IDENTIFIED BY 'CambiaEstaClave123!';
   GRANT SELECT, INSERT, UPDATE, DELETE ON siger.* TO 'siger_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Copy `.env.example` to `.env` and set real values for `DB_PASSWORD` and `JWT_SECRET`.

## Run (dev profile)

```bash
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Default seed users (from siger.sql)

The seed hashes in `siger.sql` are placeholders. After loading the schema, either
replace them with real BCrypt hashes, or register new users through `POST /api/auth/registro`.

## Tests (Testcontainers + MySQL 8)

The suite uses a real MySQL 8 container (no H2, no mocks) so the pessimistic-lock
behaviour and the `PhysicalNamingStrategyStandardImpl` camelCase mapping are
exercised exactly as in production.

### Prerequisites

- Docker running (Testcontainers launches a `mysql:8.0` container automatically).

### Run

```bash
mvn test                         # all tests
mvn test -Dtest=ReservacionServiceTest          # reservation logic (JPA slice)
mvn test -Dtest=ReservacionConcurrencyTest      # concurrency (pessimistic lock)
mvn test -Dtest=JwtServiceTest                 # JWT generation/validation
mvn test -Dtest=SecurityConfigTest             # security config (MockMvc)
```

The first run downloads the `mysql:8.0` image (~150 MB); subsequent runs reuse it.

### What is covered

| Test class | Slices | Scenarios |
|---|---|---|
| `ReservacionServiceTest` (`@DataJpaTest`) | JPA + service | happy path (reservation + notification + SIG- code), NotFoundException (event/participant), BusinessException (cancelled event, duplicate, full capacity), `misReservaciones`, `detallePorCodigo` |
| `ReservacionConcurrencyTest` (`@SpringBootTest`) | full context | 2 concurrent reservations on cap=1 event → exactly 1 success + 1 "full"; 5 concurrent reservations on cap=3 event → exactly 3 successes + 2 failures (no overbooking) |
| `JwtServiceTest` (plain JUnit) | unit | token generation + structure, claims (subject + rol), expired token, tampered signature, foreign secret, non-JWT string, short secret rejected at `@PostConstruct` |
| `SecurityConfigTest` (`@WebMvcTest`) | web + security | public routes (GET `/api/eventos`), protected routes w/o token → 401, invalid token → 401, `@PreAuthorize` role checks (PARTICIPANTE → 403, ORGANIZADOR/ADMINISTRADOR → 200), Bean Validation → 400 after auth passes |

### Why `@SpringBootTest` (and not `@DataJpaTest`) for concurrency

`@DataJpaTest` wraps every test in a single transaction that rolls back. That
makes per-entity assertions easy but **cannot** validate cross-transaction
pessimistic locks: all threads would share one `EntityManager`/transaction.
The concurrency test therefore uses `@SpringBootTest` **without**
`@Transactional` so that each `reservar()` call from a pool thread starts its
own transaction, hits `SELECT ... FOR UPDATE` on the event row, and either
acquires the lock or waits — proving the rule RF-08 / RN-04 / RN-05 holds
under real contention.

