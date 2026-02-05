# Tasks-Global (Java 17 + Spring Boot) + Angular + PostgreSQL (Docker)

Projeto completo com:
- Backend: Java 17 + Spring Boot, DDD, Flyway, Swagger/OpenAPI, validação e exclusão lógica (deleted).
- Frontend: Angular + Material + CDK Drag&Drop (Kanban) + Modal + validação.
- Banco: PostgreSQL via Docker Compose.
- Testes: JUnit 5.

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
Em outro terminal, na raiz do projeto:
```bash
cd frontend
npm install
ng serve -o
```

Frontend: http://localhost:4200

## Regras importantes

### Exclusão lógica
- Ao deletar uma task, o sistema marca `deleted=true`.

### Endpoints principais
- `POST /api/tarefas` criar
- `GET /api/tarefas?status=TODO|DOING|DONE` listar (filtro opcional)
- `PUT /api/tarefas/{id}` atualizar (campos parciais)
- `DELETE /api/tarefas/{id}` exclusão lógica

---

## Como rodar os testes (JUnit + Testcontainers)
Na raiz do projeto:
```bash
cd backend
mvn test
```
