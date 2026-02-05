# Backend (Spring Boot)

## Rodar
1) Suba o Postgres na raiz do repo:
```bash
docker compose up -d
```

2) Inicie o backend:
```bash
mvn spring-boot:run
```

Swagger UI:
- http://localhost:8080/swagger-ui.html

## Testes
```bash
mvn test
```

> Necessário Docker rodando para Testcontainers.
