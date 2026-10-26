# Core Bancario Simplificado

Sistema que simula las operaciones internas de un banco comercial: registro de clientes, apertura de cuentas y transacciones de caja o banca en linea con validacion de saldos e historial de movimientos.

Proyecto academico para la materia Programacion II (POO), Universidad Evangelica de El Salvador.

## Stack

- **Backend:** Java 21, Spring Boot 3.4.x, Maven
- **Frontend:** Angular 20
- **Seguridad:** Spring Security + JWT (stateless), roles `CLIENTE`, `CAJERO`, `GERENTE`, `ADMIN`
- **Persistencia:** PostgreSQL 16+ via Spring Data JPA (clientes, cuentas, empleados); el log de transacciones sigue en `.dat` (`backend/datos/`)
- **Tests:** perfil `test` con H2 en memoria, no requiere Docker

## Estructura

```
core-bancario/
├── backend/    # API REST en Spring Boot
├── frontend/   # Aplicacion Angular
├── docs/       # Documentacion del proyecto (incl. manual de usuario)
└── docker-compose.yml
```

## Correr todo con Docker (recomendado)

Solo necesitas Docker con Compose. Levanta Postgres, el backend y el
frontend (nginx) con un comando:

```bash
cp .env.example .env      # edita JWT_SECRET y las claves BOOTSTRAP_*
docker compose up -d --build
```

Cuando termine (el backend tarda unos segundos en arrancar):

- Aplicacion: <http://localhost:4200> (cambia el puerto con `WEB_PORT` en `.env`)
- Entra como gerente o admin con las credenciales `BOOTSTRAP_*` del `.env`
  (por defecto `00000001-1` y `00000002-2`); los clientes se registran en `/registro`.
- Los datos viven en volumenes de Docker (`pgdata` para Postgres, `datos` para el
  log de transacciones) y sobreviven a `docker compose down`.

Comandos utiles:

```bash
docker compose logs -f backend   # ver el log del backend
docker compose down              # detener (conserva los datos)
docker compose down -v           # detener y borrar todos los datos
```

El frontend sirve la API desde el mismo origen (`/api` se reenvia al backend),
asi que no hace falta configurar CORS para este modo.

## Correr sin Docker (desarrollo)

Necesitas Java 21, Maven, Node 22 y un Postgres local con la base
`core_bancario` creada.

## Variables de entorno (backend sin Docker)

El backend lee configuracion (puerto, CORS, log, Postgres, JWT y usuarios
iniciales) desde `backend/.env`, cargado automaticamente al arrancar (via
[spring-dotenv](https://github.com/paulschwarz/spring-dotenv)). Antes de
correrlo por primera vez:

```bash
cd backend
cp .env.example .env
```

`.env` no se commitea (esta en `.gitignore`); `.env.example` documenta las
variables disponibles. Cambia `JWT_SECRET` (minimo 32 caracteres) y las
claves `BOOTSTRAP_*`.

Al arrancar, si aun no existen, se crean un gerente y un administrador con
las credenciales `BOOTSTRAP_GERENTE_*` / `BOOTSTRAP_ADMIN_*`. Desde ahi:
el gerente crea cajeros y otros gerentes; los clientes se autoregistran en
`/registro`.

## Roles y pantallas

| Rol | Pantallas |
|-----|-----------|
| Cliente | `/registro`, `/mis-cuentas`, `/cuentas/:numero` (depositar, retirar, transferir, historial) |
| Cajero | `/ventanilla`, `/clientes`, `/clientes/nuevo` |
| Gerente | lo del cajero (ventanilla solo consulta) + `/empleados/nuevo` |
| Admin | `/admin/usuarios` |

## Tests

```bash
cd backend
mvn test
```

## Como correr el backend

```bash
cd backend
mvn spring-boot:run
```

## Como correr el frontend

```bash
cd frontend
npm install
ng serve
```
