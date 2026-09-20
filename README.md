# Core Bancario Simplificado

Sistema que simula las operaciones internas de un banco comercial: registro de clientes, apertura de cuentas y transacciones de caja o banca en linea con validacion de saldos e historial de movimientos.

Proyecto academico para la materia Programacion II (POO), Universidad Evangelica de El Salvador.

## Stack

- **Backend:** Java 21, Spring Boot 3.4.x, Maven
- **Frontend:** Angular 20
- **Persistencia (avance actual):** serializacion Java a archivos `.dat` (`backend/datos/`)
- **Persistencia (milestone futuro):** PostgreSQL 16+ via Spring Data JPA

## Estructura

```
core-bancario/
├── backend/    # API REST en Spring Boot
├── frontend/   # Aplicacion Angular
└── docs/       # Documentacion del proyecto
```

## Variables de entorno

El backend lee configuracion (puerto, origen CORS permitido, nivel de log)
desde `backend/.env`, cargado automaticamente al arrancar (via
[spring-dotenv](https://github.com/paulschwarz/spring-dotenv)). Antes de
correrlo por primera vez:

```bash
cd backend
cp .env.example .env
```

`.env` no se commitea (esta en `.gitignore`); `.env.example` documenta las
variables disponibles.

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
