# Tasks DDD (Java 17 + Spring Boot) + Angular Kanban + PostgreSQL (Docker)

Projeto completo com:
- Backend: Java 17 + Spring Boot, DDD, Flyway, Swagger/OpenAPI (PT-BR), validação, erros em português, exclusão lógica (deleted).
- Frontend: Angular + Material + CDK Drag&Drop (Kanban) + Modal + validação.
- Banco: PostgreSQL via Docker Compose.
- Testes: JUnit 5 + Spring Boot Test + Testcontainers (Postgres).

## Pré-requisitos (instalações)

### Obrigatórios
1. **Java 17+**
   - Verifique: `java -version`
2. **Docker / Docker Desktop**
   - Verifique: `docker version`
3. **Node.js (LTS recomendado, >= 18)**
   - Verifique: `node -v` e `npm -v`

### Recomendados
4. **Angular CLI**
   - Instalar: `npm i -g @angular/cli`
   - Verifique: `ng version`

> Para o backend, você pode usar **Maven** (`mvn`) instalado na máquina.

---

## Como rodar (passo a passo)

### 1) Subir o banco
Na raiz do projeto:
```bash
docker compose up -d
```

### 2) Rodar o backend
```bash
cd backend
mvn spring-boot:run
```

Backend: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html  
OpenAPI JSON: http://localhost:8080/v3/api-docs

### 3) Rodar o frontend
Em outro terminal:
```bash
cd frontend
npm install
ng serve -o
```

Frontend: http://localhost:4200

---N

## Regras importantes

### Exclusão lógica
- Ao deletar uma task, o sistema marca `deleted=true`.
- **Todas as consultas** ignoram tasks deletadas (não aparecem em listagem nem em busca por id).

### Endpoints principais
- `POST /api/tarefas` criar
- `GET /api/tarefas?status=TODO|DOING|DONE` listar (filtro opcional)
- `PUT /api/tarefas/{id}` atualizar (campos parciais)
- `DELETE /api/tarefas/{id}` exclusão lógica
- `DELETE /api/tarefas/{id}/hard` exclusão física (opcional)

---

## Como rodar os testes (JUnit + Testcontainers)
```bash
cd backend
./mvnw test
```

> Para testes com Testcontainers, o Docker precisa estar rodando.
