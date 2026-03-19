# Arrows Backend Microservices

This repository is structured as a microservice-based Spring Boot backend.

## Services
- api-gateway: Entry point for client requests (port 3000)
- auth-service: Authentication endpoints (port 8081)
- user-service: PostgreSQL-backed user APIs (port 8082)
- client-service: PostgreSQL-backed client and org-unit APIs (port 8083)

## Build all services
```bash
mvn -Dmaven.repo.local=.m2 clean package
```

## Run with Docker
```bash
docker compose up --build
```

## Run services locally (separate terminals)
```bash
mvn -Dmaven.repo.local=.m2 -pl auth-service spring-boot:run
mvn -Dmaven.repo.local=.m2 -pl user-service spring-boot:run
mvn -Dmaven.repo.local=.m2 -pl client-service spring-boot:run
mvn -Dmaven.repo.local=.m2 -pl api-gateway spring-boot:run
```

## Multi-Tenant Header
All tenant-backed APIs now require:
- `X-Tenant-Id: 00000000-0000-0000-0000-000000000001`

The shared tenant bootstrap seeds:
- tenant id: `00000000-0000-0000-0000-000000000001`
- tenant schema: `tenant_local`
- default org unit id: `10000000-0000-0000-0000-000000000001`

## API currently aligned with frontend contract
- POST http://localhost:3000/api/login
  request: { "email": "user@example.com", "password": "admin123" }
  success: { "token": "...", "email": "user@example.com" }
  invalid credentials: 401
- POST http://localhost:3000/api/users
  header: `X-Tenant-Id: 00000000-0000-0000-0000-000000000001`
  request: { "orgUnitId": "10000000-0000-0000-0000-000000000001", "fullName": "Demo User", "email": "demo-user@example.com", "status": "ACTIVE" }
- GET http://localhost:3000/api/users
- GET http://localhost:3000/api/users/{id}
- PUT http://localhost:3000/api/users/{id}
- DELETE http://localhost:3000/api/users/{id}
- GET http://localhost:3000/api/client/health
  success: { "status": "UP", "message": "client-service is running" }
- POST http://localhost:3000/api/org-units
  header: `X-Tenant-Id: 00000000-0000-0000-0000-000000000001`
  request: { "orgUnitName": "Engineering", "status": "ACTIVE" }
- GET http://localhost:3000/api/org-units
- POST http://localhost:3000/api/clients
  header: `X-Tenant-Id: 00000000-0000-0000-0000-000000000001`
  request: { "orgUnitId": "<orgUnitId>", "clientName": "Acme Corp", "industry": "Technology", "status": "ACTIVE" }
- GET http://localhost:3000/api/clients

## Profiles
- Available profiles: `local`, `dev`, `stag`, `perf`, `prod`
- Default profile: `local`
- Set profile while running:
```bash
mvn -Dmaven.repo.local=.m2 -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=dev
```

## Swagger / OpenAPI
- Enabled in: `local`, `dev`, `stag`, `perf`
- Disabled in: `prod`
- Swagger UI URLs:
  - http://localhost:3000/swagger-ui/index.html
  - http://localhost:8081/swagger-ui/index.html
  - http://localhost:8082/swagger-ui/index.html
  - http://localhost:8083/swagger-ui/index.html

## Client service database overrides
Set these environment variables when needed:
- CLIENT_DATASOURCE_URL (default: jdbc:postgresql://localhost:5432/jobportal)
- CLIENT_DATASOURCE_USERNAME (default: postgres)
- CLIENT_DATASOURCE_PASSWORD (default: postgres)

Client service schema management:
- Shared tenant bootstrap creates `public.tenant*`, `tenant_template`, provisions `tenant_local`, and seeds the local tenant row
- `client-service` reads and writes tenant data inside the schema resolved from `X-Tenant-Id`
- Local runs need PostgreSQL running before `client-service` starts

## User service database overrides
Set these environment variables when needed:
- USER_DATASOURCE_URL (default: jdbc:postgresql://localhost:5432/jobportal)
- USER_DATASOURCE_USERNAME (default: postgres)
- USER_DATASOURCE_PASSWORD (default: postgres)

User service schema management:
- Shared tenant bootstrap creates `public.tenant*`, `tenant_template`, provisions `tenant_local`, and seeds the local tenant row
- `user-service` now maps to tenant-scoped `user_account` and also syncs `public.tenant_user_directory`
- Local runs need PostgreSQL running before `user-service` starts

## Schema-Per-Tenant Model
The database model lives in:
- [database/schema-per-tenant/schema-per-tenant.dbml](C:/src/Arrows_Backend/database/schema-per-tenant/schema-per-tenant.dbml)
- [database/schema-per-tenant/001_public_registry.sql](C:/src/Arrows_Backend/database/schema-per-tenant/001_public_registry.sql)
- [database/schema-per-tenant/002_tenant_template.sql](C:/src/Arrows_Backend/database/schema-per-tenant/002_tenant_template.sql)
- [database/schema-per-tenant/003_provision_tenant_schema.sql](C:/src/Arrows_Backend/database/schema-per-tenant/003_provision_tenant_schema.sql)
- [database/schema-per-tenant/004_seed_local_tenant.sql](C:/src/Arrows_Backend/database/schema-per-tenant/004_seed_local_tenant.sql)

These files define the public tenant registry, the `tenant_template` schema, the provisioning function used to create tenant schemas, and the seeded local tenant that the running services use by default.
