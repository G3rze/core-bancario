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
└── docs/       # Documentacion del proyecto
```

## Variables de entorno

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
