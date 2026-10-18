# SIGER — Sistema de Gestión de Reservas

Grupo de trabajo Numero 10

Materia ADS Ciclo 2 2026 grupo teorico 1

Tutor: Lic. Edwin Aristondo Monzón González

Integrantes:

---

> Monorepo full-stack para la gestión de reservas de eventos: Spring Boot 3 (Java 21) + React 18 (Vite) + MySQL 8.

SIGER (Sistema de Gestión de Reservas En Linea) es una aplicación web para que los **organizadores** publiquen eventos con cupos limitados y los **participantes** reserven lugares, con panel de administración, autenticación JWT y notificaciones en tiempo real vía WebSockets.

---

## Tabla de contenidos (control+click Izq)

1. [Stack tecnológico](#stack-tecnológico)
2. [Estructura del monorepo](#estructura-del-monorepo)
3. [Prerrequisitos](#prerrequisitos)
4. [Puesta en marcha rápida (Docker)](#puesta-en-marcha-rápida-docker) /(falta)
5. [Puesta en marcha manual (dev local)](#puesta-en-marcha-manual-dev-local)
6. [Variables de entorno](#variables-de-entorno)
7. [Esquema de base de datos](#esquema-de-base-de-datos)
8. [Endpoints REST](#endpoints-rest)
9. [Plan de sprints](#plan-de-sprints)
10. [Flujo de trabajo Git del equipo](#flujo-de-trabajo-git-del-equipo)
11. [Errores comunes y soluciones](#errores-comunes-y-soluciones)
12. [Desarrollo con VS Code](#desarrollo-con-vs-code)
13. [Licencia](#licencia)

---

## Stack tecnológico

| Capa          | Tecnología                       | Versión / Detalle                        |
| ------------- | -------------------------------- | ---------------------------------------- |
| Backend       | Java + Spring Boot               | Java 21 LTS, Spring Boot 3.3.x           |
| ORM           | Spring Data JPA + Hibernate      | Herencia JOINED para subtipos de usuario |
| Seguridad     | Spring Security + JWT            | HS256, expiración configurable           |
| Real-time     | Spring WebSocket (STOMP)         | Notificaciones de reserva                |
| Base de datos | MySQL                            | 8.0.16+ (`utf8mb4_unicode_ci`)           |
| Migraciones   | SQL script único                 | `database/siger.sql` (origen de verdad)  |
| Frontend      | React + Vite                     | React 18, Vite 5                         |
| UI            | TailwindCSS + shadcn/ui          | Componentes accesibles                   |
| Estado        | Zustand / Context API            | Sesión JWT en memoria + `localStorage`   |
| HTTP cliente  | Axios                            | Interceptores para JWT y refresco        |
| Routing       | React Router v6                  | Rutas protegidas por rol                 |
| Empaquetado   | Maven (backend) + npm (frontend) | Wrapper incluido                         |
| Contenedores  | Docker + Docker Compose          | Multi-stage builds                       |
| CI/CD         | GitHub Actions                   | `mvn verify` + `npm run build`           |

---

## Estructura del monorepo

```
siger/
├── database/
│   └── siger.sql                  # Script SQL único (12 tablas + 1 vista + seed)
├── backend/
│   ├── Dockerfile                 # Multi-stage: maven build -> JRE runtime
│   ├── .dockerignore
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/siger/
│           │   ├── SigerApplication.java
│           │   ├── config/        # SecurityConfig, WebSocketConfig, CorsConfig
│           │   ├── controller/    # REST controllers + WebSocket controllers
│           │   ├── dto/            # Request/Response DTOs
│           │   ├── model/         # Entidades JPA (herencia JOINED)
│           │   ├── repository/    # Spring Data JPA interfaces
│           │   ├── service/       # Lógica de negocio
│           │   └── security/      # JwtFilter, JwtProvider, UserDetailsSvc
│           └── resources/
│               ├── application.properties            # perfil default
│               ├── application-dev.properties       # perfil dev
│               ├── application-prod.properties      # perfil prod
│               ├── logback-spring.xml               # logging
│               └── db/migration/README.md           # reservado para Flyway
├── frontend/ (falta o no implementado todavia * )
│   ├── Dockerfile *                # Multi-stage: node build -> nginx serve
│   ├── .dockerignore *
│   ├── nginx.conf                # SPA fallback + proxy /api
│   ├── package.jso
│   ├── vite.config.ts
│   └── src/
│       ├── api/                   # cliente Axios + endpoints
│       ├── components/            # UI reutilizable (shadcn/ui)
│       ├── context/              # AuthContext, ToastContext
│       ├── pages/                # Login, Eventos, MisReservas, Admin, Organizador
│       ├── routes/               # Rutas protegidas
│       └── lib/                  # utilidades, constantes
├── scripts/
│   ├── run-dev.sh                # Levanta MySQL + backend + frontend en dev
│   └── reset-db.sh               # Reinicia la base de datos desde siger.sql
├── .github/workflows/ci.yml      # CI en GitHub Actions
├── .gitignore
├── .env.example                  # Plantilla de variables de entorno
├── docker-compose.yml *           # Orquestación: db + backend + frontend
├── docker-compose.override.yml *   # Overrides para desarrollo (hot reload)
├── LICENSE                       # MIT
└── README.md                     # Este archivo
```

---

## Prerrequisitos

### Opción A — Docker (recomendada\*)

| Software       | Versión mínima                   |
| -------------- | -------------------------------- |
| Docker Engine  | 24.0                             |
| Docker Compose | v2.20+ (plugin `docker compose`) |

### Opción B — Dev local manual -- implementada--------

| Software     | Versión | Verificar                  |
| ------------ | ------- | -------------------------- |
| JDK          | 21 LTS  | `java -version`            |
| Maven        | 3.9+    | `mvn -v` (o usar `./mvnw`) |
| Node.js      | 20 LTS  | `node -v`                  |
| npm          | 10+     | `npm -v`                   |
| MySQL Server | 8.0.16+ | `mysql --version`          |
| Git          | 2.40+   | `git --version`            |

---

## Puesta en marcha rápida (Docker)

> Irve para revisión del cliente o despliegue de demo. Todo el stack se levanta con un comando \*.

```bash
# 1. Clonar el repositorio
git clone https://github.com/<org>/siger.git
cd siger

# 2. Crear archivo de entorno con tus secretos
cp .env.example .env
# Edita .env y cambia MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD y JWT_SECRET
# por valores seguros de al menos 32 caracteres.

# 3. Levantar todo el stack
docker compose up --build

# 4. En otra terminal, seguir logs del backend
docker compose logs -f backend
```

Servicios disponibles una vez levantado:

| Servicio    | URL local             | Credenciales por defecto (seed)         |
| ----------- | --------------------- | --------------------------------------- |
| Frontend    | http://localhost:5173 | `admin@siger.com` / `admin123`          |
| Backend API | http://localhost:8080 |                                         |
| MySQL       | localhost:3306        | `siger_app` / valor de `MYSQL_PASSWORD` |

El primer arranque de MySQL ejecuta automáticamente `database/siger.sql`
(montado en `/docker-entrypoint-initdb.d/`). El backend espera al healthcheck
`mysqladmin ping` antes de arrancar.

Para detener todo y **conservar** la base de datos:

```bash
docker compose down
```

Para detener y **borrar** la base de datos (reinit en próximo arranque):

```bash
docker compose down -v
```

---

## Puesta en marcha manual (dev local)

### Paso 1 — Base de datos MySQL

```bash
# Linux/macOS
sudo mysql -u root -p < database/siger.sql

# O usar el helper del repo:
./scripts/reset-db.sh
```

Crear usuario de aplicación (si no está en el seed):

```sql
CREATE USER 'siger_app'@'localhost' IDENTIFIED BY 'tu_password';
GRANT ALL PRIVILEGES ON siger.* TO 'siger_app'@'localhost';
FLUSH PRIVILEGES;
```

### Paso 2 — Backend (Spring Boot)

```bash
cd backend

# Variables de entorno mínimas
export DB_URL="jdbc:mysql://localhost:3306/siger?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME="siger_app"
export DB_PASSWORD="tu_password"
export JWT_SECRET="genera_un_secret_de_al_menos_64_caracteres_aqui"
export SPRING_PROFILES_ACTIVE="dev"

# Compilar y arrancar (puerto 8080)
./mvnw spring-boot:run
```

Verificar: abrir http://localhost:8080/actuator/health → `{"status":"UP"}`.

### Paso 3 — Frontend (Vite)

```bash
cd frontend
npm install
cp .env.example .env.local   # ajustar VITE_API_BASE_URL si backend no está en 8080
npm run dev                   # http://localhost:5173
```

### Helper todo-en-uno

```bash
./scripts/run-dev.sh   # verifica MySQL, exporta env por defecto, lanza backend+frontend
```

---

## Variables de entorno

| Variable                 | Servicio    | Descripción                           | Ejemplo                          |
| ------------------------ | ----------- | ------------------------------------- | -------------------------------- |
| `MYSQL_DATABASE`         | db          | Nombre de la BD (debe ser `siger`)    | `siger`                          |
| `MYSQL_USER`             | db          | Usuario de aplicación                 | `siger_app`                      |
| `MYSQL_PASSWORD`         | db, backend | Password de aplicación                | string seguro                    |
| `MYSQL_ROOT_PASSWORD`    | db          | Password root (solo init)             | string seguro                    |
| `DB_URL`                 | backend     | JDBC URL                              | `jdbc:mysql://db:3306/siger?...` |
| `DB_USERNAME`            | backend     | Usuario JDBC                          | `siger_app`                      |
| `DB_PASSWORD`            | backend     | Password JDBC (inyectado por compose) |                                  |
| `JWT_SECRET`             | backend     | Clave de firma HS256 (≥64 chars)      | string aleatorio                 |
| `JWT_EXPIRATION_MS`      | backend     | Expiración del token                  | `86400000`                       |
| `SPRING_PROFILES_ACTIVE` | backend     | Perfil activo                         | `dev` / `prod`                   |
| `VITE_API_BASE_URL`      | frontend    | URL del backend desde el navegador    | `http://localhost:8080`          |
| `FRONTEND_PORT`          | compose     | Puerto público del frontend           | `5173`                           |

> **Seguridad:** nunca commitees un `.env` con secretos reales. El archivo
> `.gitignore` ya excluye `.env`, `.env.local` y variantes. En producción,
> inyecta los secretos desde un gestor (Vault, AWS Secrets Manager, GitHub
> Actions secrets, etc.).

---

## Esquema de base de datos

Definido en `database/siger.sql` (origen de verdad único, 12 tablas):

| #   | Tabla           | Tipo                            |
| --- | --------------- | ------------------------------- |
| 1   | `usuario`       | Base de herencia JOINED         |
| 2   | `administrador` | Subtipo de `usuario`            |
| 3   | `organizador`   | Subtipo de `usuario`            |
| 4   | `participante`  | Subtipo de `usuario`            |
| 5   | `categoria`     | Catálogo                        |
| 6   | `evento`        | Entidad principal               |
| 7   | `sede`          | Catálogo                        |
| 8   | `sesion`        | Horarios por evento             |
| 9   | `reserva`       | Inscripción participante-sesión |
| 10  | `notificacion`  | Mensajes a usuario              |
| 11  | `auditoria`     | Bitácora de cambios             |
| 12  | `configuracion` | Params globales                 |

Más 1 vista (`vw_resumen_eventos`) y datos semilla (usuarios, categorías, sede demo).

Convenciones:

- Columnas en **camelCase** (coincide con entidades JPA).
- Charset `utf8mb4`, collation `utf8mb4_unicode_ci`.
- FK con `ON DELETE CASCADE` en subtipos.
- `AUTO_INCREMENT` en todas las PK.

---

## Endpoints REST

Base path: `/api`. Autenticación vía `Authorization: Bearer <JWT>` salvo en `/auth/**` y `/public/**`.

### Autenticación

| Método | Ruta                 | Descripción                   |
| ------ | -------------------- | ----------------------------- |
| POST   | `/api/auth/login`    | Inicia sesión y devuelve JWT  |
| POST   | `/api/auth/register` | Registro de participante      |
| GET    | `/api/auth/me`       | Datos del usuario autenticado |
| POST   | `/api/auth/refresh`  | Refresca token                |

### Usuarios (admin)

| Método | Ruta                 | Rol   | Descripción                   |
| ------ | -------------------- | ----- | ----------------------------- |
| GET    | `/api/usuarios`      | ADMIN | Lista paginada                |
| GET    | `/api/usuarios/{id}` | ADMIN | Detalle                       |
| POST   | `/api/usuarios`      | ADMIN | Crear organizador/admin       |
| PUT    | `/api/usuarios/{id}` | ADMIN | Actualizar                    |
| DELETE | `/api/usuarios/{id}` | ADMIN | Borrado lógico (estado=false) |

### Eventos

| Método | Ruta                         | Rol        | Descripción           |
| ------ | ---------------------------- | ---------- | --------------------- |
| GET    | `/api/eventos`               | público    | Lista eventos activos |
| GET    | `/api/eventos/{id}`          | público    | Detalle con sesiones  |
| POST   | `/api/eventos`               | ORG, ADMIN | Crear                 |
| PUT    | `/api/eventos/{id}`          | ORG, ADMIN | Actualizar            |
| POST   | `/api/eventos/{id}/sesiones` | ORG        | Añadir sesión         |
| PATCH  | `/api/eventos/{id}/publicar` | ADMIN      | Aprobar publicación   |

### Reservas

| Método | Ruta                 | Rol        | Descripción             |
| ------ | -------------------- | ---------- | ----------------------- |
| POST   | `/api/reservas`      | PART       | Reservar cupo en sesión |
| GET    | `/api/reservas/mias` | PART       | Mis reservas            |
| DELETE | `/api/reservas/{id}` | PART       | Cancelar                |
| GET    | `/api/reservas`      | ADMIN, ORG | Listado por evento      |

### WebSocket (STOMP)

- Endpoint: `/ws-notificaciones` (SockJS)
- Topic: `/topic/notificaciones/{userId}` — notificaciones en tiempo real al usuario.

### Utilidad

| Método | Ruta               | Descripción           |
| ------ | ------------------ | --------------------- |
| GET    | `/actuator/health` | Healthcheck (no auth) |

---

## Plan de sprints

| Sprint | Objetivo                                                          | Entregable                                                   |
| ------ | ----------------------------------------------------------------- | ------------------------------------------------------------ |
| S0     | Setup repo, Docker, CI, esquema DB                                | Este repo + `siger.sql` levantándose con `docker compose up` |
| S1     | Auth JWT + login UI + sesión React                                | `/auth/login`, `AuthContext`, rutas protegidas               |
| S2     | CRUD eventos (organizador) + catálogo público                     | `/eventos`, formulario creación, lista pública               |
| S3     | Reservas + cupos + WebSockets de notificación                     | `/reservas`, `/ws-notificaciones`, panel "Mis reservas"      |
| S4     | Panel admin (usuarios, aprobación, auditoría)                     | `/admin/*`, `vw_resumen_eventos`                             |
| S5     | Hardening: tests, rate-limit, logs estructurados, despliegue prod | CI verde, Docker images en registry, README final            |

---

## Flujo de trabajo Git del equipo

### Convención de ramas (Git Flow simplificado)

| Rama                    | Propósito                                  |
| ----------------------- | ------------------------------------------ |
| `main`                  | Siempre deployable. Tags de release.       |
| `develop`               | Integración de features del sprint actual. |
| `feature/<US-XXX>-slug` | Una historia de usuario por rama.          |
| `bugfix/<BUG-XXX>-slug` | Corrección de bug.                         |
| `hotfix/<HH-XXX>-slug`  | Parche urgente sobre `main`.               |

### Convención de commits (Conventional Commits en español)

```
<tipo>(<scope>): <descripción>

<cuerpo opcional>

<footer opcional: closes #issue>
```

Tipos válidos: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`, `perf`.

Ejemplo:

```
feat(reservas): validar cupo disponible antes de crear reserva

Impide reservar si aforoDisponible <= 0. Lanza ReservaNoDisponibleException
que el frontend muestra como toast de error.

closes #S3-12
```

### Flujo de Pull Request

1. Crear rama desde `develop`: `git checkout -b feature/S2-13-formulario-evento develop`.
2. Commits pequeños y atómicos con la convención anterior.
3. `git push -u origin feature/S2-13-formulario-evento`.
4. Abrir PR contra `develop`:
   - Plantilla de descripción (qué cambia, cómo probar, capturas si aplica).
   - Al menos **un aprobador** del equipo (revisión cruzada).
   - CI debe estar en verde (`mvn -B verify` y `npm ci && npm run build`).
   - Sin conflictos con `develop` (rebase si hace falta).
5. Merge con **squash** para mantener historial limpio.
6. Borrar rama de origen automáticamente.

### Release

1. Desde `develop`, abrir PR a `main` con título `release vX.Y.Z`.
2. Tras merge, etiquetar `main` con `vX.Y.Z` (`git tag -a vX.Y.Z -m "..."`).
3. CI publica imágenes Docker a `ghcr.io/<org>/siger-backend:vX.Y.Z` y `:latest`.
4. Actualizar `CHANGELOG.md` (mantenido a mano por ahora).

---

## Errores comunes y soluciones

### 1. `Communications link failure` al arrancar el backend

**Causa:** MySQL aún no está listo cuando Spring intenta conectar.
**Solución:** En Docker ya está resuelto con `depends_on: db: condition: service_healthy`.
En dev local, arrancar MySQL primero y verificar con `mysqladmin -u root -p ping`.

### 2. `Access denied for user 'siger_app'@'...'`

**Causa:** El usuario no existe o la password no coincide con `DB_PASSWORD`.
**Solución:**

```sql
ALTER USER 'siger_app'@'%' IDENTIFIED BY 'nueva_password';
FLUSH PRIVILEGES;
```

Reiniciar el backend. Si usas Docker, asegúrate de que `MYSQL_PASSWORD` y `DB_PASSWORD` coinciden.

### 3. El frontend no llega al backend (CORS)

**Causa:** Spring no permite el origen de Vite.
**Solución:** Verificar `CorsConfig` permite `http://localhost:5173` en perfil dev. El `nginx.conf` del frontend también hace proxy de `/api` a `backend:8080`, evitando CORS en producción.

### 4. `Public Key Retrieval is not allowed`

**Causa:** MySQL 8 + JDBC sin SSL.
**Solución:** Añadir `allowPublicKeyRetrieval=true` a la JDBC URL (ya incluido en `.env.example`).

### 5. `node_modules` pesado o build lento en Docker

**Causa:** Falta cache de capas.
**Solución:** El `Dockerfile` del frontend copia primero `package*.json` y corre `npm ci` antes de copiar el resto del código, maximizando el cache de capas. Si cambias dependencias, el rebuild será lento solo la primera vez.

### 6. El volumen de MySQL no reinicia el SQL

**Causa:** `/docker-entrypoint-initdb.d/` solo se ejecuta la **primera vez** que se inicializa el volumen.
**Solución:**

```bash
docker compose down -v   # borra el volumen
docker compose up --build
```

### 7. Puerto 8080 o 5173 ya en uso

**Causa:** Otro proceso local ocupa el puerto.
**Solución:**

```bash
lsof -i :8080   # macOS/Linux
# o
netstat -tulpn | grep 8080
```

Matar el proceso o cambiar el mapeo en `docker-compose.yml` (p. ej. `8081:8080`).

### 8. WebSockets no conectan

**Causa:** Proxy/nginx corta upgrade de protocolo.
**Solución:** En nginx.conf del frontend, asegurarse de que `proxy_set_header Upgrade $http_upgrade;` y `Connection "upgrade";` están presentes para `/ws-notificaciones`. En dev local, Vite proxy debe tener `ws: true`.

### 9. `JWT signature does not match`

**Causa:** El `JWT_SECRET` cambió entre reinicios (tokens antiguos ya no validan).
**Solución:** Mantener el mismo `JWT_SECRET` entre reinicios. Si se rota, todos los usuarios deben volver a loguearse (esperado).

### 10. Tests en CI fallan por falta de MySQL

**Causa:** CI necesita base de datos para tests de integración.
**Solución:** Configurar Testcontainers en `pom.xml` (futuro sprint S5) o usar H2 en perfil `test` para tests unitarios.

---

## Notificaciones en tiempo real (WebSocket/STOMP)

Cuando un PARTICIPANTE confirma una reservación, el backend **empuja la notificación
en vivo** al navegador del usuario mediante STOMP sobre WebSocket (sin necesidad de
que recargue la página).

### Flujo

1. El frontend abre un socket SockJS+STOMP a `http://localhost:8080/ws-notificaciones`
   enviando el JWT como cabecera `Authorization: Bearer <token>` en el frame `CONNECT`.
2. El `JwtChannelInterceptor` valida el JWT con el mismo `JwtService` del REST y fija
   el `Principal` (email) en la sesión STOMP. Tokens inválidos rechazan el `CONNECT`.
3. Al confirmar una reservación, `ReservacionService` guarda la `Notificacion` en BD y
   llama a `NotificacionStompService.enviar(n)`, que hace
   `simpMessagingTemplate.convertAndSendToUser(email, "/queue/notificaciones", dto)`.
4. Spring resuelve el destino del usuario → `/user/queue/notificaciones` → el cliente
   recibe el frame, incrementa el contador de no leídas y muestra un _toast_.
5. Si no hay cliente conectado, el push es _no-op_ (la notificación queda en BD y se ve
   vía `GET /api/notificaciones/mis`). El push está envuelto en `try/catch` para que un
   fallo de WS **nunca** rompa la reservación.

### Archivos

| Lado     | Archivo                                 | Rol                                                                                       |
| -------- | --------------------------------------- | ----------------------------------------------------------------------------------------- |
| Backend  | `config/WebSocketConfig.java`           | `@EnableWebSocketMessageBroker`, endpoint `/ws-notificaciones` con SockJS, prefix `/user` |
| Backend  | `security/JwtChannelInterceptor.java`   | Valida JWT en `CONNECT`, fija el `Principal`                                              |
| Backend  | `service/NotificacionStompService.java` | `convertAndSendToUser(email, ...)`                                                        |
| Backend  | `service/ReservacionService.java`       | Llama al push tras guardar la notificación (best-effort)                                  |
| Frontend | `api/ws.js`                             | Cliente SockJS+STOMP (`@stomp/stompjs` + `sockjs-client`)                                 |
| Frontend | `context/NotificacionContext.jsx`       | Conexión reactiva, contador `noLeidas`, última notificación                               |
| Frontend | `components/Navbar.jsx`                 | Campana 🔔 + badge + indicador de conexión + toast                                        |

### Probarlo

1. Arranca backend (`F5` o tarea **Backend: Run**) + frontend (tarea **Frontend: Run**).
2. Inicia sesión como **participante** en una pestaña.
3. En otra sesión como **organizador** (o vía `requests.http`), reserva en un evento.
4. Verás en la pestaña del participante: un **toast** "Tu reservación… fue confirmada…"
   y el badge de la campana 🔔 incrementa a `1`.

---

## Desarrollo con VS Code

El monorepo incluye configuración lista para VS Code en [`.vscode/`](./.vscode):
[`extensions.json`](./.vscode/extensions.json) (extensiones recomendadas),
[`settings.json`](./.vscode/settings.json) (Java 21, formateo, exclusiones),
[`launch.json`](./.vscode/launch.json) (debug Spring Boot + React + full-stack) y
[`tasks.json`](./.vscode/tasks.json) (tareas para correr, testear y construir).

### Prerrequisitos (instalar una sola vez)

| Herramienta                | Versión  | Verificar                                                 |
| -------------------------- | -------- | --------------------------------------------------------- |
| JDK 21 (Temurin u OpenJDK) | 21 LTS   | `java -version`                                           |
| Maven                      | 3.9+     | `mvn -v`                                                  |
| Node.js                    | 20 LTS+  | `node -v`                                                 |
| MySQL                      | 8.0.16+  | `mysql --version`                                         |
| Docker Desktop             | reciente | `docker --version` (para Testcontainers y docker-compose) |
| VS Code                    | reciente | —                                                         |

> Si no tienes Maven instalado, abre el backend y ejecuta la tarea
> **"Backend: Generar wrapper de Maven (mvnw)"** (requiere Maven una sola vez) para crear `./mvnw`.

### Extensiones recomendadas

Al abrir la carpeta `siger/` por primera vez, VS Code ofrece instalar las
extensiones de [`extensions.json`](./.vscode/extensions.json). Acepta todas.
Las esenciales:

- **Extension Pack for Java** (`vscjava.vscode-java-pack`) — lenguaje Java + Maven + debugger + Test Runner.
- **Spring Boot Extension Pack** (`vmware.vscode-spring-boot-pack`) — autocompletado de `application.properties`, navegación a beans.
- **ESLint** + **Prettier** — para el frontend React.
- **Docker** — gestionar contenedores y imágenes desde VS Code.
- **MySQL** (`cweijan.vscode-mysql-client2`) — explorar la base `siger` sin salir del editor.
- **REST Client** (`humao.restclient`) — probar la API desde [`.vscode/requests.http`](./.vscode/requests.http).

### Flujo de trabajo típico

1. **Abrir el proyecto:** `code siger/` (o _File → Open Folder_ sobre la carpeta `siger`).
2. **(Solo la primera vez) Cargar la BD:** ejecuta la tarea **"DB: Reset (cargar siger.sql)"** o desde terminal `mysql -u root -p < database/siger.sql`.
3. **(Solo la primera vez) Instalar dependencias del frontend:** tarea **"Frontend: Install (npm install)"**.
4. **Arrancar el backend:** `F5` (debug con breakpoints) o tarea **"Backend: Run (./mvnw spring-boot:run)"**. La API queda en `http://localhost:8080`.
   > Las tareas usan `./mvnw` (wrapper de Maven incluido en `backend/`); la primera vez descarga Maven 3.9.9 (~10 MB) y lo cachea en `~/.m2/wrapper/`. **No necesitas Maven instalado** en el sistema.
5. **Arrancar el frontend:** tarea **"Frontend: Run (npm run dev)"**. El dev server queda en `http://localhost:5173`.
6. **Depurar full-stack:** usa la configuración compuesta **"Full-stack: Backend + Frontend"** en el panel _Run and Debug_ (lanza el backend con debugger y abre Chrome contra el frontend).
7. **Probar la API sin Postman:** abre [`.vscode/requests.http`](./.vscode/requests.http), pega tu JWT en la variable `@token_*` y haz clic en _Send Request_ sobre cada bloque `###`.
8. **Correr tests:** `Ctrl+Shift+P` → _Tasks: Run Task_ → elige el test específico (Reservacion / Concurrencia / Jwt / Security), o usa la tarea **"Backend: Tests (./mvnw test)"** para toda la suite. También puedes lanzar un test individual con el icono ▶ sobre cada `@Test` (cortesía de la _Test Runner for Java_).
9. **Depurar un test paso a paso:** ejecuta la tarea **"Backend: Debug tests Surefire (suspende en puerto 8000)"**, espera el mensaje `Listening for transport dt_socket at address: 8000`, y luego `F5` con la config **"Backend: Spring Boot (Attach al puerto 8000)"** para conectar el debugger. Pon breakpoints en el `@Test` y continúa.

### Atajos útiles

| Acción                             | Atajo                                                       |
| ---------------------------------- | ----------------------------------------------------------- |
| Run / Debug (backend o full-stack) | `F5`                                                        |
| Run Task                           | `Ctrl+Shift+B` (build) o `Ctrl+Shift+P` → _Tasks: Run Task_ |
| Abrir terminals                    | `Ctrl+\``                                                   |
| Test Explorer                      | barra lateral → icono de matraz (Tests)                     |
| Send REST request                  | sobre un bloque `###` en `requests.http` → _Send Request_   |

### Notas

- Los secretos (`DB_PASSWORD`, `JWT_SECRET`) están como valores por defecto en
  `launch.json` y `tasks.json` **solo para desarrollo local**. Para un entorno
  compartido, muévelos a un `.env` (ya ignorado por `.gitignore`) o a las
  variables de entorno del sistema.
- Si el _Java Language Server_ no detecta el JDK 21, abre la paleta de comandos
  (`Ctrl+Shift+P`) → _Java: Configure Java Runtime_ y selecciona el JDK 21.

---

## Licencia

Distribuido bajo licencia **MIT**. Ver [`LICENSE`](./LICENSE) para el texto completo.

---

## Contribuir

1. Lee [Flujo de trabajo Git del equipo](#flujo-de-trabajo-git-del-equipo).
2. Abre un issue describiendo el cambio antes de programar.
3. Sigue las convenciones de commits y PR.
4. Verifica que `mvn -B verify` y `npm ci && npm run build` pasen en local antes de abrir PR.

> Hecho con esfuerzo por el Grupo 10 de ADS bajo el tema SIGER - sistemade registro y reserva de eventos.

> a tener en cuenta si se mueve el projecto de lugar hay que limpiar cache ya que los caches (target/, node_modules/) pueden quedar con rutas viejas.

cd .....\Siger (lugar donde se movio o ruta actual)

# Borra caches (se regeneran solos al recompilar)

Remove-Item -Recurse -Force backend\target -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force frontend\node_modules -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force frontend\dist -ErrorAction SilentlyContinue

# Reinstala dependencias del frontend

cd frontend
npm install
cd ..

# Recompila el backend (descarga todo de nuevo en ~/.m2, no se toca)

cd backend
mvn clean compile "-Dmaven.test.skip=true"

> Arrancá el backend (con la nueva ruta)

cd ......\Siger\backend
$env:DB_PASSWORD = "CambiaEstaClave123!"
$env:JWT_SECRET = "clave-secreta-de-desarrollo-de-al-menos-32-caracteres-siger"
mvn spring-boot:run "-Dmaven.test.skip=true"

> Arrancá el frontend (otra terminal).

cd .......\Siger\frontend
npm run dev

> debe aparecer:

VITE v5.4.21 ready in 835 ms

➜ Local: http://localhost:..../
➜ Network: use --host to expose
➜ press h + enter to show help
